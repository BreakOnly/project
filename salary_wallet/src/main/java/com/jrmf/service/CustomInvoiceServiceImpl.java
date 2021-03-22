package com.jrmf.service;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.CustomInvoiceIsDefault;
import com.jrmf.domain.CustomInvoiceInfoDO;
import com.jrmf.domain.Page;
import com.jrmf.domain.vo.CustomInvoiceInfoVO;
import com.jrmf.persistence.CustomInvoiceInfoDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/13 17:38
 * Version:1.0
 *
 * @author guoto
 */
@Service("customInvoiceService")
public class CustomInvoiceServiceImpl implements CustomInvoiceService {

    private final CustomInvoiceInfoDao customInvoiceInfoDao;

    @Autowired
    public CustomInvoiceServiceImpl(CustomInvoiceInfoDao customInvoiceInfoDao) {
        this.customInvoiceInfoDao = customInvoiceInfoDao;
    }

    @Override
    public boolean addCustomInvoiceInfo(CustomInvoiceInfoVO customInvoiceInfoVO) {
        List<CustomInvoiceInfoDO> customInvoiceInfoDOS = customInvoiceInfoDao.listCustomInvoiceInfo(customInvoiceInfoVO.getCustomkey());
        CustomInvoiceInfoDO customInvoiceInfoDO = customInvoiceVO2DO(customInvoiceInfoVO);
        // 默认为有效的状态
        customInvoiceInfoDO.setStatus(CommonString.EFFECTIVE);
        if (customInvoiceInfoDOS == null || customInvoiceInfoDOS.size() == 0) {
            // 当初次添加收货地址时,设置为默认收货地址
            customInvoiceInfoDO.setIsDefault(CustomInvoiceIsDefault.IS_DEFAULT.getCode());
        } else {
            // 默认为非默认收货地址
            customInvoiceInfoDO.setIsDefault(CustomInvoiceIsDefault.NOT_DEFAULT.getCode());
        }
        int i = customInvoiceInfoDao.insertCustomInvoiceInfo(customInvoiceInfoDO);
        return (i != 0 && customInvoiceInfoDO.getId() != 0);
    }

    @Override
    public List<CustomInvoiceInfoVO> listCustomInvoiceInfo(String customkey) {
        List<CustomInvoiceInfoDO> customInvoiceInfoDOS = customInvoiceInfoDao.listCustomInvoiceInfo(customkey);
        ArrayList<CustomInvoiceInfoVO> customInvoiceInfoVOS = new ArrayList<>();
        for (CustomInvoiceInfoDO customInvoiceInfoDO : customInvoiceInfoDOS) {
            customInvoiceInfoVOS.add(customInvoiceDO2VO(customInvoiceInfoDO));
        }
        return customInvoiceInfoVOS;
    }

    @Override
    public List<CustomInvoiceInfoVO> listCustomInvoiceInfoByParams(HashMap<String, Object> params) {
        List<CustomInvoiceInfoDO> customInvoiceInfoDOS = customInvoiceInfoDao.listCustomInvoiceInfoByParams(params);
        ArrayList<CustomInvoiceInfoVO> customInvoiceInfoVOS = new ArrayList<>();
        for (CustomInvoiceInfoDO customInvoiceInfoDO : customInvoiceInfoDOS) {
            customInvoiceInfoVOS.add(customInvoiceDO2VO(customInvoiceInfoDO));
        }
        return customInvoiceInfoVOS;
    }

    @Override
    public boolean deleteCustomInvoiceInfo(String customkey, int id) {
        CustomInvoiceInfoDO customInvoiceInfoDOById = getCustomInvoiceInfoDOById(id);
        /*如果要删除默认收货地址，将最新的一条设置为默认收货地址。当成功删除节点时返回成功。*/
        boolean b;
        if (customInvoiceInfoDOById.getIsDefault().equals(CustomInvoiceIsDefault.IS_DEFAULT.getCode())) {
            b = customInvoiceInfoDao.deleteCustomInvoiceInfo(id) == 1;
            /*删除完成后获取节点列表，如果还有，就把最新的一条设置为默认收货地址。*/
            List<CustomInvoiceInfoDO> customInvoiceInfoDOS = customInvoiceInfoDao.listCustomInvoiceInfo(customkey);
            if (customInvoiceInfoDOS != null && customInvoiceInfoDOS.size() != 0) {
                CustomInvoiceInfoDO customInvoiceInfoDO = customInvoiceInfoDOS.get(0);
                setCurrentDefault(customInvoiceInfoDO.getCustomkey(), customInvoiceInfoDO.getId());
            }
        } else {
            b = customInvoiceInfoDao.deleteCustomInvoiceInfo(id) == 1;
        }
        return b;
    }

    @Override
    public CustomInvoiceInfoVO getCustomInvoiceInfoVOById(int id) {
        CustomInvoiceInfoDO customInvoiceInfoById = customInvoiceInfoDao.getCustomInvoiceInfoById(id);
        return customInvoiceDO2VO(customInvoiceInfoById);
    }

    @Override
    public CustomInvoiceInfoDO getCustomInvoiceInfoDOById(int id) {
        return customInvoiceInfoDao.getCustomInvoiceInfoById(id);
    }

    @Override
    public boolean updateCustomInvoiceByParam(CustomInvoiceInfoVO customInvoiceInfoVO) {
        CustomInvoiceInfoDO customInvoiceInfoDO = customInvoiceVO2DO(customInvoiceInfoVO);
        return customInvoiceInfoDao.updateCustomInvoiceByParam(customInvoiceInfoDO) == 1;
    }

    @Override
    public boolean setCurrentDefault(String customKey, int id) {
        int i = customInvoiceInfoDao.setCurrentDefault(id);
        customInvoiceInfoDao.setOtherNotDefault(customKey, id);
        return i == 1;
    }

    private CustomInvoiceInfoVO customInvoiceDO2VO(CustomInvoiceInfoDO customInvoiceInfoDO) {
        CustomInvoiceInfoVO customInvoiceInfoVO = new CustomInvoiceInfoVO();
        /*暂时不展示创建时间和修改时间*/
        customInvoiceInfoVO.setId(customInvoiceInfoDO.getId());
        customInvoiceInfoVO.setInvoicePhone(customInvoiceInfoDO.getInvoicePhone());
        customInvoiceInfoVO.setInvoiceAddress(customInvoiceInfoDO.getInvoiceAddress());
        customInvoiceInfoVO.setInvoiceUserName(customInvoiceInfoDO.getInvoiceUserName());

        customInvoiceInfoVO.setFixedTelephone(customInvoiceInfoDO.getFixedTelephone());
        customInvoiceInfoVO.setEmail(customInvoiceInfoDO.getEmail());
        customInvoiceInfoVO.setIsDefault(customInvoiceInfoDO.getIsDefault());
        return customInvoiceInfoVO;
    }

    private CustomInvoiceInfoDO customInvoiceVO2DO(CustomInvoiceInfoVO customInvoiceInfoVO) {
        CustomInvoiceInfoDO customInvoiceInfoDO = new CustomInvoiceInfoDO();
        /*暂时只需要这么写个字段*/
        customInvoiceInfoDO.setId(customInvoiceInfoVO.getId());
        customInvoiceInfoDO.setCustomkey(customInvoiceInfoVO.getCustomkey());
        customInvoiceInfoDO.setInvoicePhone(customInvoiceInfoVO.getInvoicePhone());
        customInvoiceInfoDO.setInvoiceUserName(customInvoiceInfoVO.getInvoiceUserName());
        customInvoiceInfoDO.setInvoiceAddress(customInvoiceInfoVO.getInvoiceAddress());
        customInvoiceInfoDO.setAddUser(customInvoiceInfoVO.getAddUser());
        customInvoiceInfoDO.setFixedTelephone(customInvoiceInfoVO.getFixedTelephone());
        customInvoiceInfoDO.setEmail(customInvoiceInfoVO.getEmail());
        return customInvoiceInfoDO;
    }

	@Override
	public List<CustomInvoiceInfoVO> listCustomInvoiceInfoByPage(Page page) {
		return customInvoiceInfoDao.listCustomInvoiceInfoByPage(page);
	}

	@Override
	public int listCustomInvoiceInfoCountByPage(Page page) {
		return customInvoiceInfoDao.listCustomInvoiceInfoCountByPage(page);
	}

	@Override
	public int getMerchantInvoiceAddressCount(Page page) {
		return customInvoiceInfoDao.getMerchantInvoiceAddressCount(page);
	}

	@Override
	public List<Map<String, Object>> getMerchantInvoiceAddressByPage(Page page) {
		return customInvoiceInfoDao.getMerchantInvoiceAddressByPage(page);
	}

	@Override
	public List<Map<String, Object>> getMerchantInvoiceAddressNoPage(Page page) {
		return customInvoiceInfoDao.getMerchantInvoiceAddressNoPage(page);
	}

    @Override
    public String getInvoicedAmountByParam(Map<String, Object> paramMap) {
        return customInvoiceInfoDao.getInvoicedAmountByParam(paramMap);
    }
}
