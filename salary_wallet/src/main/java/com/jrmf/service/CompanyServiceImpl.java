package com.jrmf.service;

import com.jrmf.domain.ChannelConfig;
import com.jrmf.domain.Company;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.SMSChannelConfig;
import com.jrmf.persistence.CompanyDao;
import com.jrmf.persistence.UserRelatedDao;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author 种路路
* @version 创建时间：2017年9月8日 下午4:05:27
* 类说明
*/
@Service("companyService")
public class CompanyServiceImpl implements CompanyService {

	@Autowired
	private CompanyDao companyDao;

	@Autowired
	private UserRelatedDao userRelatedDao;
	@Autowired
	private CustomThirdPaymentConfigService customThirdPaymentConfigService;

	@Override
	public void creatCompanyInfo(Company company) {
		companyDao.creatCompanyInfo(company);
	}
	@Override
	public Company getCompanyByUserId(int id) {
		return companyDao.getCompanyByUserId(id);
	}
	@Override
	public void addPicturePath(Company company) {
		companyDao.addPicturePath( company);
	}
	@Override
	public void updateCompanyInfo(Company company) {
		companyDao.updateCompanyInfo(company);
	}
	@Override
	public List<Company> getCompanyList(Map<String, Object> params) {
		return companyDao.getCompanyList(params);
	}

	@Override
	public List<Company> getCompanyListByProxy(String customKey) {
		return companyDao.getCompanyListByProxy(customKey);
	}

	@Override
	public PaymentConfig getPaymentConfigInfo(String paymentType,
			String originalId,
			String companyId) {
		PaymentConfig paymentConfigCustom = userRelatedDao.getPaymentConfigByTypeOriginalId(paymentType, originalId, companyId);
		if(paymentConfigCustom == null){//取默认（服务公司-通道）
			paymentConfigCustom = userRelatedDao.getPaymentConfigByTypeCompanyId(paymentType, companyId);
		}
		return paymentConfigCustom;
	}
	@Override
	public PaymentConfig getPaymentConfigInfo(String paymentType,
			String originalId, String companyId, String pathNo) {
	PaymentConfig paymentConfigCustom = userRelatedDao.getPaymentConfigTwo(paymentType, originalId, companyId,pathNo);
	if(paymentConfigCustom == null){//取默认（服务公司-通道）
		paymentConfigCustom = userRelatedDao.getPaymentConfigCompanyTwo(paymentType, companyId,pathNo);
	}
	return paymentConfigCustom;
	}

	@Override
	public List<PaymentConfig> getSubAccountPaymentConfig() {
		return userRelatedDao.getSubAccountPaymentConfig();
	}

	@Override
	public List<Company> getCompanyByUserIds(String ids) {
		return companyDao.getCompanyByUserIds(ids);
	}

	/**
	 * 查询公司名称
	 * @param companyName
	 * @return
	 */
	@Override
	public boolean getCompanyName(String companyName) {
		int conmanyNum = companyDao.getCompanyName(companyName);
		return conmanyNum > 0;
	}

	/**
	 * 新增下发公司
	 * @param company
	 * @return
	 */
	@Override
	public void addCompany(Company company) {
		companyDao.addCompany(company);
	}

	/**
	 *  查询下发公司邮箱
	 * @param email
	 * @return
	 */
	@Override
	public Company getCompanyEmail(String email) {
		Company companyEmail = companyDao.getCompanyEmail(email);
		return companyEmail;
	}

	@Override
	public int getSubAccountList(String companyId) {
		return userRelatedDao.getSubAccountList(companyId);
	}

	/**
	 * 条件查询下发公司
	 * @param paramMap
	 * @return
	 */
	@Override
	public List<Company> getCompanyListByParam(Map<String, Object> paramMap) {
		return companyDao.getCompanyListByParam(paramMap);
	}

	/**
	 * 通过服务公司名称查询服务公司信息
	 * @param companyName
	 * @return
	 */
	@Override
	public Company getCompanyByCompanyName(String companyName) {
		return companyDao.getCompanyByCompanyName(companyName);
	}

	@Override
	public String getMerchantIdByUserId(Integer companyId) {
		return companyDao.getMerchantIdByUserId(companyId);
	}
	@Override
	public int checkIsExist(ChannelConfig channelConfig) {
		return companyDao.checkIsExist(channelConfig);
	}

	/**
	 * 条件查询下发公司总数
	 * @param paramMap
	 * @return
	 */
	@Override
	public int getCompanyListByParamCount(Map<String, Object> paramMap) {
		return companyDao.getCompanyListByParamCount(paramMap);
	}

	@Override
	public SMSChannelConfig getSmsConfig() {
		return companyDao.getSmsConfig();
	}

	@Override
	public List<Company> getAllCompanyList(Integer companyType) {
		return companyDao.getAllCompanyList(companyType);
	}

	@Override
	public Company getCompanyById(String id) {
		return companyDao.getCompanyById(id);
	}

	@Override
	public PaymentConfig getPaymentConfigInfoPlus(String paymentType, String originalId,
			String companyId, String realCompanyId) {
		PaymentConfig paymentConfigCustom = userRelatedDao
				.getPaymentConfigByTypeOriginalId(paymentType, originalId, realCompanyId);
		//取默认（服务公司-通道）
		if (paymentConfigCustom == null) {
			paymentConfigCustom = userRelatedDao
					.getPaymentConfigByTypeCompanyId(paymentType, realCompanyId);
		}
		//TODO ???
		if (paymentConfigCustom != null) {
			paymentConfigCustom = customThirdPaymentConfigService
					.getConfigBySubcontract(originalId, companyId, realCompanyId, paymentConfigCustom);
		}
		return paymentConfigCustom;
	}

	@Override
	public PaymentConfig getPaymentConfigInfoPlus(String paymentType, String originalId,
			String companyId, String realCompanyId, String pathNo) {
		PaymentConfig paymentConfigCustom = userRelatedDao
				.getPaymentConfigTwo(paymentType, originalId, realCompanyId, pathNo);
		//取默认（服务公司-通道）
		if (paymentConfigCustom == null) {
			paymentConfigCustom = userRelatedDao
					.getPaymentConfigCompanyTwo(paymentType, realCompanyId, pathNo);
		}

		if (paymentConfigCustom != null) {
			paymentConfigCustom = customThirdPaymentConfigService
					.getConfigBySubcontract(originalId, companyId, realCompanyId, paymentConfigCustom);
		}
		return paymentConfigCustom;
	}

	@Override
	public Integer getCompanyCountByIdAndCompanyType(String companyId, int companyType) {
		return companyDao.getCompanyCountByIdAndCompanyType(companyId, companyType);
	}

	@Override
	public List<Company> listRealityCompany() {
		return companyDao.listRealityCompany();
	}

	@Override
	public List<Company> selectCompanyByPlatform(Integer customId) {
		return companyDao.selectCompanyByPlatform(customId);
	}

	@Override
	public Company getLikeCompanyByCompanyName(String companyName) {
		return companyDao.getLikeCompanyByCompanyName(companyName);
	}

	@Override
	public Map<String, String> getCompanyPayChannelRelation(String companyId, String pathNo) {
		return companyDao.getCompanyPayChannelRelation(companyId,pathNo);
	}

	@Override
	public List<Company> getIndividualCompanys(String userId) {
		return companyDao.getIndividualCompanys(userId);
	}
}
