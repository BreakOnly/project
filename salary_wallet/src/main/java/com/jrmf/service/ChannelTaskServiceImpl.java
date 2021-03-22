package com.jrmf.service;

import com.jrmf.controller.constant.TaskPartitionStatus;
import com.jrmf.controller.constant.TaskStatus;
import com.jrmf.domain.ChannelAreas;
import com.jrmf.domain.ChannelTask;
import com.jrmf.domain.ChannelTaskType;
import com.jrmf.domain.TaxCode;
import com.jrmf.domain.UserCommission;
import com.jrmf.persistence.ChannelTaskDao;
import com.jrmf.persistence.TaxCodeDao;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChannelTaskServiceImpl implements ChannelTaskService {

  private static Logger logger = LoggerFactory.getLogger(ChannelTaskService.class);

  @Autowired
  private ChannelTaskDao channelTaskDao;
  @Autowired
  private UserCommissionService commissionService;
  @Autowired
  TaxCodeDao taxCodeDao;

  @Override
  public int deleteByPrimaryKey(Integer id) {
    return channelTaskDao.deleteByPrimaryKey(id);
  }

  @Override
  public int insert(ChannelTask task) {
    return channelTaskDao.insert(task);
  }

  @Override
  public int insertSelective(ChannelTask record) {
    return channelTaskDao.insertSelective(record);
  }

  @Override
  public ChannelTask selectByPrimaryKey(Integer id) {
    return channelTaskDao.selectByPrimaryKey(id);
  }

  @Override
  public int updateByPrimaryKey(ChannelTask task) {
    return channelTaskDao.updateByPrimaryKey(task);
  }

  @Override
  public int updateByPrimaryKeySelective(ChannelTask task) {
    return channelTaskDao.updateByPrimaryKeySelective(task);
  }

  @Override
  public List<ChannelTask> selectAll(Map<String, Object> paramMap) {
    return channelTaskDao.selectAll(paramMap);
  }

  @Override
  public int updateConfirmMatchTask(String certIds) {
    return channelTaskDao.updateConfirmMatchTask(certIds);
  }

  @Override
  public List<String> selectResourceCustom(String customKeys) {
    return channelTaskDao.selectResourceCustom(customKeys);
  }

  @Override
  public void autogenerateTask(String customKeys, String startTime, String endTime,
      String startAmount, String endAmount, String taskIds, String orderNos) {
    logger.info("---------开始生成结算任务---------");

    Map<String, Object> paramMap = new HashMap<>();

    //可生成结算任务的商户
    List<String> customKeyList = selectResourceCustom(customKeys);
    if (customKeyList != null && customKeyList.size() > 0) {
      Random rand = new Random();
      Integer taskPartition = TaskPartitionStatus.AUTOTASK.getCode();
      Integer status = TaskStatus.TOBECONFIRM.getCode();
      for (String customKey : customKeyList) {
        Date date = new Date();
        if (StringUtil.isEmpty(startTime)) {
          startTime = StringUtil.formatDate(date, "yyyy-MM-dd");
        }
        if (StringUtil.isEmpty(endTime)) {
          endTime = StringUtil.formatDate(date, "yyyy-MM-dd");
        }
        //需要生成的结算任务的明细
        List<UserCommission> commissionList = commissionService
            .getAutogenerateTaskList(startTime, endTime, customKey, startAmount, endAmount,
                orderNos);
        List<ChannelTask> taskList = null;

        paramMap.put("customKey", customKey);
        paramMap.put("taskPartition", TaskPartitionStatus.RESOURCETASK.getCode());
        if (!StringUtil.isEmpty(taskIds)) {
          paramMap.put("taskIds", taskIds);
          taskList = channelTaskDao.selectAutogenerateTaskAll(paramMap);
        }
        if (commissionList != null && commissionList.size() > 0) {
          logger.info("开始生成customKey为{}商户的任务,生成数量:{}", customKey, commissionList.size());
          for (UserCommission commission : commissionList) {
            if (StringUtil.isEmpty(taskIds)) {
              paramMap.put("amount", commission.getAmount());
              //该商户所有的资源任务
              taskList = channelTaskDao.selectAutogenerateTaskAll(paramMap);
            }
            if (taskList == null || taskList.size() == 0) {
              logger.error("orderNo:{}未生成结算任务", commission.getOrderNo());
              continue;
            }
            List<ChannelTask> fitChannelTask = getFitChannelTask(taskList,
                commission.getAmount());
            if (fitChannelTask.isEmpty()) {
              ChannelTask randomTask = taskList.get(rand.nextInt(taskList.size()));
              fitChannelTask.add(randomTask);
            }
            int taskSize = fitChannelTask.size();
            String firstTaskAmount = "";
            for (int i = 0; i < fitChannelTask.size(); i++) {
              ChannelTask channelTask = fitChannelTask.get(i);
              ChannelTask newTask = new ChannelTask(channelTask);
              newTask.setCustomKey(customKey);
              newTask.setTaskPartition(taskPartition);
              newTask.setUndertakerName(commission.getUserName());
              newTask.setUndertakerCertId(commission.getCertId());
              newTask.setStatus(status);
              newTask.setPublishTime(commission.getCreatetime());
              newTask.setStartTime(commission.getCreatetime());
              newTask.setEndTime(commission.getCreatetime());
              if (taskSize == 2) {
                String realAmount;
                if (i == 0) {
                  String otherFee = ArithmeticUtil
                      .modStr2(commission.getAmount(), newTask.getUnitPrice());
                  realAmount = ArithmeticUtil.subStr2(commission.getAmount(), otherFee);
                  firstTaskAmount = realAmount;
                } else {
                  realAmount = ArithmeticUtil.subStr2(commission.getAmount(), firstTaskAmount);
                }
                newTask.setTaskAmount(realAmount);
                newTask
                    .setTaskAchievement(ArithmeticUtil.modStr(realAmount, newTask.getUnitPrice()));
                newTask.setOtherFee(ArithmeticUtil.modStr2(realAmount, newTask.getUnitPrice()));
                newTask
                    .setAchievementFee(ArithmeticUtil.subStr2(realAmount, newTask.getOtherFee()));
              } else {
                newTask.setTaskAmount(commission.getAmount());
                newTask.setTaskAchievement(
                    ArithmeticUtil.modStr(commission.getAmount(), newTask.getUnitPrice()));
                newTask.setOtherFee(
                    ArithmeticUtil.modStr2(commission.getAmount(), newTask.getUnitPrice()));
                newTask.setAchievementFee(
                    ArithmeticUtil.subStr2(newTask.getTaskAmount(), newTask.getOtherFee()));
              }
              newTask.setLinkPhoneNo(commission.getPhoneNo());
              newTask.setOrderNo(commission.getOrderNo());
              newTask.setPayType(commission.getPayType());
              newTask.setPaymentTime(commission.getPaymentTime());
              newTask.setAccount(commission.getAccount());
              newTask.setBankName(commission.getBankName());
              newTask.setCompanyId(commission.getCompanyId());
              channelTaskDao.insert(newTask);
            }
            commissionService.updateIsTask(commission.getId());
            logger.info("orderNo:{}的明细生成结算任务成功", commission.getOrderNo());
          }
        }
      }
    }
  }

  private List<ChannelTask> getFitChannelTask(List<ChannelTask> channelTasks, String amount) {
    List<ChannelTask> resultTask = new ArrayList<>();
    //任务按照单价从高到低排序
    Collections.sort(channelTasks, new Comparator<ChannelTask>() {
      @Override
      public int compare(ChannelTask o1, ChannelTask o2) {
        return ArithmeticUtil.compareTod(o2.getUnitPrice(), o1.getUnitPrice());
      }
    });
    //获取单价小于下发金额且单价最大的任务
    for (ChannelTask channelTask : channelTasks) {
      String unitPrice = channelTask.getUnitPrice();
      if (ArithmeticUtil.compareTod(amount, unitPrice) >= 0) {
        resultTask.add(channelTask);
        if (resultTask.size() == 1) {
          amount = ArithmeticUtil.modStr2(amount, unitPrice);
        }
        if (resultTask.size() == 2) {
          break;
        }
      }
    }
    return resultTask;
  }

  @Override
  public List<ChannelTask> selectCustomAll(Map<String, Object> paramMap) {
    return channelTaskDao.selectCustomAll(paramMap);
  }

  @Override
  public List<ChannelTaskType> selectAllType() {
    return channelTaskDao.selectAllType();
  }

  @Override
  public List<ChannelTask> selectResourceAll(Map<String, Object> paramMap) {
    return channelTaskDao.selectResourceAll(paramMap);
  }

  @Override
  public List<ChannelAreas> selectByParentCode(String parentCode) {
    return channelTaskDao.selectByParentCode(parentCode);
  }

  @Override
  public int updateTaskStatus(Integer id, Integer status) {
    return channelTaskDao.updateTaskStatus(id, status);
  }

  @Override
  public List<ChannelTask> selectAutogenerateTaskAll(Map<String, Object> paramMap) {
    return channelTaskDao.selectAutogenerateTaskAll(paramMap);
  }

  @Override
  public int selectCustomAllCount(Map<String, Object> paramMap) {
    return channelTaskDao.selectCustomAllCount(paramMap);
  }

  @Override
  public List<TaxCode> selectTaxCode(int level, String levelCode) {
    try {
      List<TaxCode> taxCodeList = taxCodeDao
          .listTaxCode("level" + level, "level" + (level + 1), levelCode);
      return taxCodeList;
    } catch (Exception e) {
      logger.error("查询税收编码异常", e);
    }
    return null;
  }
}
