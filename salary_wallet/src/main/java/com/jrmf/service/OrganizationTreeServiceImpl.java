package com.jrmf.service;

import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.systemrole.SystemRoleController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.OrganizationNode;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.persistence.CustomGroupDao;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.utils.StringUtil;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/28 15:10
 * Version:1.0
 *
 * @author guoto
 */
@Service("organizationTreeService")
public class OrganizationTreeServiceImpl implements OrganizationTreeService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationTreeServiceImpl.class);

    private final CustomGroupDao customGroupDao;
    private final CustomProxyDao customProxyDao;
    private final ChannelCustomDao channelCustomDao;
    private final ChannelRelatedDao channelRelatedDao;

    @Autowired
    public OrganizationTreeServiceImpl(CustomGroupDao customGroupDao, CustomProxyDao customProxyDao, ChannelCustomDao channelCustomDao, ChannelRelatedDao channelRelatedDao) {
        this.customGroupDao = customGroupDao;
        this.customProxyDao = customProxyDao;
        this.channelCustomDao = channelCustomDao;
        this.channelRelatedDao = channelRelatedDao;
    }


    @Override
    public List<OrganizationNode> queryOrganizationTree(String customKey, int customType, String queryMode, int nodeId,Integer platformId) {
        List<OrganizationNode> nodes;
        switch (queryMode) {
            case QueryType.QUERY_ALL:
                nodes = queryNodesAll(customType,platformId);
                break;
            case QueryType.QUERY_CURRENT:
                nodes = queryNodesCurrent(customType, nodeId,platformId);
                break;
            case QueryType.QUERY_CHILD_NODE:
                nodes = queryNodesChild(customType, nodeId,platformId);
                break;
            case QueryType.QUERY_CHILD_NODE_LEVEL_ONE:
                nodes = queryNodesChildLevelOne(customType, nodeId, customKey,platformId);
                break;
            case QueryType.QUERY_CURRENT_AND_CHILDREN:
                nodes = queryNodesCurrentAndChildren(customType, nodeId,platformId);
                break;
            case QueryType.QUERY_CURRENT_AND_CHILDREN_LEVEL_ONE:
                nodes = queryNodesCurrentAndChildrenLevelOne(customType, nodeId,platformId);
                break;
            case QueryType.QUERY_PROXY_TREE:
                nodes = queryProxyTree(platformId);
                break;
            default:
                //普通商户:id = nodeId
                nodes = qeruyNodesByDefault(customKey, customType,platformId);
        }
        return nodes;
    }

    private List<OrganizationNode> queryProxyTree(Integer platformId) {
        return customProxyDao.queryProxyTree(platformId);
    }

    private List<OrganizationNode> queryNodesAll(int customType,Integer platformId) {
        List<OrganizationNode> nodes = new ArrayList<>();
        switch (customType) {
            case QueryType.CUSTOM_GROUP:
                nodes = customGroupDao.listAll(platformId);
                break;
            case QueryType.CUSTOM_PROXY:
                nodes = customProxyDao.listAll(platformId);
                break;
            case QueryType.COMPANY:
                nodes = channelCustomDao.getAllCompany(platformId);
                break;
            default:
        }
        return nodes;
    }

    private List<OrganizationNode> queryNodesCurrentAndChildrenLevelOne(int customType, int nodeId,Integer platformId) {
        List<OrganizationNode> nodes = new ArrayList<>();
        OrganizationNode nodeById;
        switch (customType) {
            case QueryType.CUSTOM_GROUP:
                nodeById = customGroupDao.getNodeById(nodeId,platformId);
                nodes = customGroupDao.queryCurrentAndChildrenNodesLevelOne(nodeById.getLevelCode(), nodeById.getContentLevel(),platformId);
                break;
            case QueryType.CUSTOM_PROXY:
                nodeById = customGroupDao.getNodeById(nodeId,platformId);
                nodes = customProxyDao.queryCurrentAndChildrenNodesLevelOne(nodeById.getLevelCode(), nodeById.getContentLevel(),platformId);
                break;
            default:
        }
        return nodes;
    }

    private List<OrganizationNode> queryNodesCurrentAndChildren(int customType, int nodeId, Integer platformId) {
        List<OrganizationNode> nodes = new ArrayList<>();
        OrganizationNode nodeById;
        switch (customType) {
            case QueryType.CUSTOM_GROUP:
                nodeById = customGroupDao.getNodeById(nodeId,platformId);
                nodes = customGroupDao.queryNodesCurrentAndChildren(nodeById.getLevelCode(),platformId);
                break;
            case QueryType.CUSTOM_PROXY:
                nodeById = customProxyDao.getNodeById(nodeId,platformId);
                nodes = customProxyDao.queryNodesCurrentAndChildren(nodeById.getLevelCode(),platformId);
                break;
            default:
        }
        return nodes;
    }

    private List<OrganizationNode> queryNodesChildLevelOne(int customType, int nodeId, String customKey,Integer platformId) {
        List<OrganizationNode> nodes = new ArrayList<>();
        OrganizationNode nodeById;
        switch (customType) {
            case QueryType.CUSTOM_GROUP:
                nodeById = customGroupDao.getNodeById(nodeId,platformId);
                nodes = customGroupDao.queryNodesChildLevelOne(nodeById.getLevelCode(), nodeById.getContentLevel(),platformId);
                break;
            case QueryType.CUSTOM_PROXY:
                nodeById = customProxyDao.getNodeById(nodeId,platformId);
                nodes = customProxyDao.queryNodesChildLevelOne(nodeById.getLevelCode(), nodeById.getContentLevel(),platformId);
                for (OrganizationNode node : nodes) {
                    if (node.getCustomType() == 5) {
                        nodeById = customGroupDao.getNodeByCustomKey(node.getCustomKey(),platformId);
                        node.setId(nodeById.getId());
                        node.setHasChilden(nodeById.getHasChilden());
                        node.setCustomKey(nodeById.getCustomKey());
                    }
                }
                break;
            case QueryType.CUSTOM_PROXY_CHILDEN:
                nodeById = customProxyDao.getNodeByCustomKey(customKey,null);
                OrganizationNode proxyChildenNodeById = customProxyDao.getProxyChildenNodeByCustomKey(customKey,platformId);
                nodes = customProxyDao.queryProxyChildenNodesChildLevelOne(nodeById.getLevelCode(), nodeById.getContentLevel(), proxyChildenNodeById.getLevelCode(), proxyChildenNodeById.getContentLevel());
                //如果查询出来的数据中含有集团性商户,就关联查询group表,把节点的id等信息更新为group表的
                for (OrganizationNode node : nodes) {
                    if (node.getCustomType() == 5) {
                        nodeById = customGroupDao.getNodeByCustomKey(node.getCustomKey(),platformId);
                        node.setId(nodeById.getId());
                        node.setHasChilden(nodeById.getHasChilden());
                        node.setCustomKey(nodeById.getCustomKey());
                    }
                }
                //如果查询出来的数据中含有代理商并且该代理商没有子节点,就关联查询proxy表,查询proxy表的数据有没有子节点
                for (OrganizationNode node : nodes) {
                    if (node.getCustomType() == 3 && node.getHasChilden() == -1) {
                        nodeById = customProxyDao.getNodeByCustomKey(node.getCustomKey(),platformId);
                        node.setId(nodeById.getId());
                        node.setHasChilden(nodeById.getHasChilden());
                        node.setCustomKey(nodeById.getCustomKey());
                    }
                }
                break;
            default:
        }
        return nodes;
    }

    private List<OrganizationNode> queryNodesChild(int customType, int nodeId,Integer platformId) {
        List<OrganizationNode> nodes;
        OrganizationNode nodeById;
        switch (customType) {
            case QueryType.CUSTOM_GROUP:
                nodeById = customGroupDao.getNodeById(nodeId,platformId);
                nodes = customGroupDao.queryChild(nodeById.getLevelCode(), nodeId,platformId);
                break;
            case QueryType.CUSTOM_PROXY:
                nodeById = customProxyDao.getNodeById(nodeId,platformId);
                nodes = customProxyDao.queryChild(nodeById.getLevelCode(), nodeId,platformId);
                break;
            default:
                nodes = new ArrayList<>();
        }
        return nodes;
    }

    private List<OrganizationNode> qeruyNodesByDefault(String customKey, int customType,Integer platformId) {
        List<OrganizationNode> nodes = new ArrayList<>();
        OrganizationNode node;
        ChannelCustom custom;
        switch (customType) {
            case QueryType.CUSTOM:
                custom = channelCustomDao.getCustomByCustomkey(customKey,platformId);
                node = new OrganizationNode();
                node.setId(custom.getId());
                node.setOrganizationName(custom.getCompanyName());
                node.setContentLevel(1);
                node.setHasChilden(-1);
                node.setCustomType(CustomType.CUSTOM.getCode());
                node.setCustomKey(customKey);
                nodes.add(node);
                break;
            case QueryType.CUSTOM_GROUP:
                node = customGroupDao.getNodeByCustomKey(customKey,platformId);
                nodes.add(node);
                break;
            case QueryType.CUSTOM_PROXY:
                node = customProxyDao.getNodeByCustomKey(customKey,platformId);
                nodes.add(node);
                break;
            case QueryType.COMPANY:
                custom = channelCustomDao.getCustomByCustomkey(customKey,platformId);
                node = new OrganizationNode();
                node.setId(custom.getId());
                node.setOrganizationName(custom.getCompanyName());
                node.setContentLevel(1);
                node.setHasChilden(-1);
                node.setCustomType(CustomType.COMPANY.getCode());
                node.setCustomKey(customKey);
                nodes.add(node);
                break;
            case QueryType.CUSTOM_PLATFORM_CHILDEN:
                custom = channelCustomDao.getCustomByCustomkey(customKey,platformId);
                node = new OrganizationNode();
                node.setId(custom.getId());
                node.setOrganizationName(custom.getCompanyName());
                node.setContentLevel(1);
                node.setHasChilden(-1);
                node.setCustomType(CustomType.CUSTOM.getCode());
                node.setCustomKey(customKey);
                nodes.add(node);
                break;
            default:
        }
        return nodes;
    }

    private List<OrganizationNode> queryNodesCurrent(int customType, int nodeId,Integer platformId) {
        List<OrganizationNode> nodes = new ArrayList<>();
        OrganizationNode nodeById;
        switch (customType) {
            case QueryType.CUSTOM_GROUP:
                nodeById = customGroupDao.getNodeById(nodeId,platformId);
                nodes.add(nodeById);
                break;
            case QueryType.CUSTOM_PROXY:
                nodeById = customProxyDao.getNodeById(nodeId,platformId);
                nodes.add(nodeById);
                break;
            case QueryType.COMPANY:
                nodeById = channelCustomDao.getNodeById(nodeId,platformId);
                nodes.add(nodeById);
                break;
            default:
                nodes = new ArrayList<>();
        }
        return nodes;
    }

    @Override
    public List<String> queryNodeCusotmKey(int customType, String queryMode, int nodeId) {
        List<String> customKeys = null;
        switch (customType) {
            case QueryType.CUSTOM:
                customKeys = queryCusotmKeysFromCustom(nodeId);
                break;
            case QueryType.CUSTOM_GROUP:
                customKeys = queryCusotmKeysFromGourp(queryMode, nodeId);
                break;
            case QueryType.CUSTOM_PROXY:
                customKeys = queryCusotmKeysFromProxy(queryMode, nodeId);
                break;
            case QueryType.COMPANY:
                customKeys = queryCusotmKeysFromCompany(queryMode, nodeId);
                break;
            case QueryType.CUSTOM_PROXY_CHILDEN:
                customKeys = queryCusotmKeysFromProxyChilden(queryMode, nodeId);
                break;
            default:
        }
        return customKeys;
    }

    private List<String> queryCusotmKeysFromCompany(String queryMode, int nodeId) {
        ChannelCustom custom = channelCustomDao.getCustomById(nodeId);
        return channelRelatedDao.queryCustomKeysByCompanyId(custom.getCustomkey());
    }

    private List<String> queryCusotmKeysFromProxy(String queryMode, int nodeId) {
        OrganizationNode nodeById = customProxyDao.getNodeById(nodeId,null);
        List<String> customKeys = new ArrayList<>();
        switch (queryMode) {
            case QueryType.QUERY_CURRENT_AND_CHILDREN:
                customKeys = customProxyDao.queryProxyCurrentAndChildrenCustomkeys(nodeById.getLevelCode());
                break;
            case QueryType.QUERY_CURRENT:
                customKeys.add(nodeById.getCustomKey());
                break;
            default:
        }
        return customKeys;
    }

    private List<String> queryCusotmKeysFromGourp(String queryMode, int nodeId) {
        OrganizationNode nodeById = customGroupDao.getNodeById(nodeId,null);
        List<String> customKeys = new ArrayList<>();
        switch (queryMode) {
            case QueryType.QUERY_CURRENT_AND_CHILDREN:
                customKeys = customGroupDao.queryGroupCurrentAndChildrenCustomkeys(nodeById.getLevelCode());
                break;
            case QueryType.QUERY_CURRENT:
                customKeys.add(nodeById.getCustomKey());
                break;
            default:
        }
        return customKeys;
    }

    private List<String> queryCusotmKeysFromCustom(int nodeId) {
        List<String> customKeys = new ArrayList<>();
        OrganizationNode node = customProxyDao.getNodeById(nodeId,null);
        if (node == null) {
            ChannelCustom customById = channelCustomDao.getCustomById(nodeId);
            customKeys.add(customById.getCustomkey());
        } else {
            customKeys.add(node.getCustomKey());
        }
        return customKeys;
    }

    public List<String> queryNodeCustomKeyListByChannelCustomId(int customType, String queryMode, int channelCustomId){
        ChannelCustom channelCustom= channelCustomDao.getCustomById(channelCustomId);
        if(channelCustom==null){
            return Collections.emptyList();
        }
        int nodeId=queryNodeIdByCustomKey(channelCustom.getCustomkey());
        return queryNodeCusotmKey(customType,queryMode,nodeId);
    }

    @Override
    public int queryNodeIdByCustomKey(String customKey) {

        int nodeId = 0;
        ChannelCustom custom = channelCustomDao.getCustomByCustomkey(customKey,null);

        int customType = custom.getCustomType();
        OrganizationNode organizationNode = null;
        if (customType == 3) {
            organizationNode = customProxyDao.getNodeByCustomKey(customKey,null);
        } else if (customType == 5) {
            organizationNode = customGroupDao.getNodeByCustomKey(customKey,null);
        } else {
            organizationNode = new OrganizationNode();
        }

        nodeId = organizationNode.getId();

        return nodeId;
    }

    @Override
    public boolean addProxy(String customKey) {
        OrganizationNode node = new OrganizationNode();
        node.setCustomKey(customKey);
        node.setParentId(0);
        node.setRegType(1);
        node.setEnable(1);
        node.setHasChilden(-1);
        node.setContentLevel(1);
        if (customProxyDao.insertProxy(node) == 1) {
            node.setLevelCode(node.getId() + "-");
            return customProxyDao.updateNodeById(node) == 1;
        }
        return false;
    }

    @Override
    public boolean addGroup(String customKey) {
        OrganizationNode node = new OrganizationNode();
        node.setCustomKey(customKey);
        node.setParentId(0);
        node.setRegType(1);
        node.setEnable(1);
        node.setHasChilden(-1);
        node.setContentLevel(1);
        if (customGroupDao.insertGroup(node) == 1) {
            node.setLevelCode(node.getId() + "-");
            return customGroupDao.updateNodeById(node) == 1;
        }
        return false;
    }

    @Override
    public boolean addParentProxy(String parentCustomKey, String childCustomKey) {
        OrganizationNode proxy = customProxyDao.getNodeByCustomKey(parentCustomKey,null);
        OrganizationNode childNode = customProxyDao.getNodeByCustomKey(childCustomKey,null);
        if (childNode == null) {
            OrganizationNode node = new OrganizationNode();
            node.setHasChilden(-1);
            node.setRegType(1);
            node.setEnable(1);
            node.setCustomKey(childCustomKey);
            node.setContentLevel(proxy.getContentLevel() + 1);
            node.setParentId(proxy.getId());
            if (customProxyDao.insertProxy(node) == 1) {
                node.setLevelCode(proxy.getLevelCode() + node.getId() + "-");
                customProxyDao.updateNodeById(node);
                if (proxy.getHasChilden() != 1) {
                    proxy.setHasChilden(1);
                    customProxyDao.updateNodeById(proxy);
                }
                return true;
            }
            return false;
        } else {
            childNode.setLevelCode(proxy.getLevelCode() + childNode.getId() + "-");
            childNode.setParentId(proxy.getId());
            childNode.setContentLevel(proxy.getContentLevel() + 1);
            if (customProxyDao.updateNodeById(childNode) == 1) {
                if (proxy.getHasChilden() != 1) {
                    proxy.setHasChilden(1);
                    customProxyDao.updateNodeById(proxy);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addParentGroup(String parentGroupCustomKey, String childCustomKey) {
        OrganizationNode parentNode = customGroupDao.getNodeByCustomKey(parentGroupCustomKey,null);
        OrganizationNode childNode = customGroupDao.getNodeByCustomKey(childCustomKey,null);
        if (childNode == null) {
            OrganizationNode node = new OrganizationNode();
            node.setHasChilden(-1);
            node.setRegType(1);
            node.setEnable(1);
            node.setCustomKey(childCustomKey);
            node.setContentLevel(parentNode.getContentLevel() + 1);
            node.setParentId(parentNode.getId());
            if (customGroupDao.insertGroup(node) == 1) {
                node.setLevelCode(parentNode.getLevelCode() + node.getId() + "-");
                customGroupDao.updateNodeById(node);
                if (parentNode.getHasChilden() != 1) {
                    parentNode.setHasChilden(1);
                    customGroupDao.updateNodeById(parentNode);
                }
                return true;
            }
            return false;
        } else {
            if (childNode.getHasChilden() == 1) {
                String oldLevelCode = childNode.getLevelCode();
                int oldContentLevel = childNode.getContentLevel();
                List<OrganizationNode> nodes = customGroupDao.queryChild(childNode.getLevelCode(), childNode.getId(),null);
                childNode.setLevelCode(parentNode.getLevelCode() + childNode.getId() + "-");
                childNode.setParentId(parentNode.getId());
                childNode.setContentLevel(parentNode.getContentLevel() + 1);
                if (customGroupDao.updateNodeById(childNode) == 1) {
                    for (OrganizationNode node : nodes) {
                        node.setLevelCode(node.getLevelCode().replaceAll(oldLevelCode, childNode.getLevelCode()));
                        node.setContentLevel(node.getContentLevel() + (childNode.getContentLevel() - oldContentLevel));
                        customGroupDao.updateNodeById(node);
                    }
                    if (parentNode.getHasChilden() != 1) {
                        parentNode.setHasChilden(1);
                        customGroupDao.updateNodeById(parentNode);
                    }
                    return true;
                }
            } else {
                childNode.setLevelCode(parentNode.getLevelCode() + childNode.getId() + "-");
                childNode.setParentId(parentNode.getId());
                childNode.setContentLevel(parentNode.getContentLevel() + 1);
                if (customGroupDao.updateNodeById(childNode) == 1) {
                    if (parentNode.getHasChilden() != 1) {
                        parentNode.setHasChilden(1);
                        customGroupDao.updateNodeById(parentNode);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean removeParentGroup(String childCustomKey) {
        OrganizationNode childNode = customGroupDao.getNodeByCustomKey(childCustomKey,null);
        if (childNode != null) {
            if (childNode.getParentId() != 0) {
                //执行remove操作
                if (childNode.getHasChilden() == -1) {
                    //没有子节点，直接删除这个节点
                    customGroupDao.removeById(childNode.getId(), childNode.getId() + "-");
//                    if (childNode.getParentId() != 0) {
                    OrganizationNode parentNode = customGroupDao.getNodeById(childNode.getParentId(),null);
                    List<OrganizationNode> nodes = customGroupDao.queryChild(parentNode.getLevelCode(), parentNode.getId(),null);
                    if (nodes == null || nodes.size() == 0) {
                        parentNode.setHasChilden(-1);
                        customGroupDao.updateNodeById(parentNode);
                    }
//                    }
                } else {
                    //有子节点，更新levelCode.
                    int oldContentLevel = childNode.getContentLevel();
                    String oldLevelCode = childNode.getLevelCode();
                    List<OrganizationNode> nodes = customGroupDao.queryChild(childNode.getLevelCode(), childNode.getId(),null);
                    childNode.setLevelCode(childNode.getId() + "-");
                    childNode.setContentLevel(1);
                    if (customGroupDao.updateNodeById(childNode) == 1) {
                        for (OrganizationNode node : nodes) {
                            node.setLevelCode(node.getLevelCode().replaceAll(oldLevelCode, childNode.getLevelCode()));
                            node.setContentLevel(node.getContentLevel() + (childNode.getContentLevel() - oldContentLevel));
                            customGroupDao.updateNodeById(node);
                        }
                    }
                }
                OrganizationNode parentNode = customGroupDao.getNodeById(childNode.getParentId(),null);
                List<OrganizationNode> organizationNodes = customGroupDao.queryChild(parentNode.getLevelCode(), parentNode.getId(),null);
                if (organizationNodes == null) {
                    parentNode.setHasChilden(-1);
                    customGroupDao.updateNodeById(parentNode);
                }

                //删除父级商户后把本节点的parentId更改为0
                childNode.setParentId(0);
                customGroupDao.updateNodeParentIdById(childNode);
            }

        } else {
            addGroup(childCustomKey);
        }
        return true;
    }

    @Override
    public boolean removeParentProxy(String childCustomKey) {
        ChannelCustom custom = channelCustomDao.getCustomByCustomkey(childCustomKey,null);
        OrganizationNode childNode = customProxyDao.getNodeByCustomKey(childCustomKey,null);
        if (childNode == null) {
            channelCustomDao.removeAgentIdById(custom.getId());
            return true;
        }
        if (childNode.getParentId() != 0) {
            int oldParentId = childNode.getParentId();
            if (childNode.getHasChilden() == -1) {
                //没有子节点,直接删除当前节点
                customProxyDao.removeById(childNode.getId());
            } else {
                //有子节点，更新levelCode.
                int oldContentLevel = childNode.getContentLevel();
                String oldLevelCode = childNode.getLevelCode();
                List<OrganizationNode> nodes = customProxyDao.queryChild(childNode.getLevelCode(), childNode.getId(),null);
                childNode.setLevelCode(childNode.getId() + "-");
                childNode.setContentLevel(1);
                childNode.setParentId(0);
                if (customProxyDao.updateNodeById(childNode) == 1) {
                    for (OrganizationNode node : nodes) {
                        node.setLevelCode(node.getLevelCode().replaceAll(oldLevelCode, childNode.getLevelCode()));
                        node.setContentLevel(node.getContentLevel() + (childNode.getContentLevel() - oldContentLevel));
                        customProxyDao.updateNodeById(node);
                    }
                }
            }

            OrganizationNode parentNode = customProxyDao.getNodeById(oldParentId,null);
            List<OrganizationNode> organizationNodes = customProxyDao.queryChild(parentNode.getLevelCode(), parentNode.getId(),null);
            if (organizationNodes.isEmpty() && organizationNodes.size() == 0) {
                parentNode.setHasChilden(-1);
                customProxyDao.updateNodeById(parentNode);
            }

            if (custom != null) {
                channelCustomDao.removeAgentIdById(custom.getId());
            }
        }
        return true;
    }

    @Override
    public boolean addProxyChilden(String customKey) {
        OrganizationNode node = new OrganizationNode();
        node.setCustomKey(customKey);
        node.setParentId(0);
        node.setRegType(1);
        node.setEnable(1);
        node.setHasChilden(-1);
        node.setContentLevel(1);
        if (customProxyDao.insertProxyChilden(node) == 1) {
            node.setLevelCode(node.getId() + "-");
            return customProxyDao.updateProxyChildenNodeById(node) == 1;
        }
        return false;
    }

    @Override
    public boolean addParentProxyChilden(String parentCustomKey, String childCustomKey) {
        if (StringUtil.isEmpty(parentCustomKey)) {
            return false;
        }

        OrganizationNode proxy = customProxyDao.getProxyChildenNodeByCustomKey(parentCustomKey,null);
        if (proxy == null) {
            addProxyChilden(parentCustomKey);
            proxy = customProxyDao.getProxyChildenNodeByCustomKey(parentCustomKey,null);
        }
        OrganizationNode childNode = customProxyDao.getProxyChildenNodeByCustomKey(childCustomKey,null);
        if (childNode == null) {
            OrganizationNode node = new OrganizationNode();
            node.setHasChilden(-1);
            node.setRegType(1);
            node.setEnable(1);
            node.setCustomKey(childCustomKey);
            node.setContentLevel(proxy.getContentLevel() + 1);
            node.setParentId(proxy.getId());
            if (customProxyDao.insertProxyChilden(node) == 1) {
                node.setLevelCode(proxy.getLevelCode() + node.getId() + "-");
                customProxyDao.updateProxyChildenNodeById(node);
                if (proxy.getHasChilden() != 1) {
                    proxy.setHasChilden(1);
                    customProxyDao.updateProxyChildenNodeById(proxy);
                }
                return true;
            }
            return false;
        } else {
            // 查询所修改商户的下级节点
            List<OrganizationNode> organizationNodeList = customProxyDao.queryProxyChildenChild(childNode.getLevelCode(), childNode.getId());
            if (organizationNodeList.size() > 0 && !organizationNodeList.isEmpty()) {
                for (OrganizationNode list : organizationNodeList) {
                    if (proxy.getId() == list.getId()) {
                        return false;
                    }
                }
                for (OrganizationNode list : organizationNodeList) {
                    OrganizationNode o = customProxyDao.getProxyChildenNodeByCustomKey(list.getCustomKey(),null);
                    o.setLevelCode(proxy.getId() + "-" + childNode.getId() + "-" + list.getId() + "-");
                    customProxyDao.updateProxyChildenNodeById(o);
                }
            }
            childNode.setLevelCode(proxy.getLevelCode() + childNode.getId() + "-");
            childNode.setParentId(proxy.getId());
            childNode.setContentLevel(proxy.getContentLevel() + 1);
            if (customProxyDao.updateProxyChildenNodeById(childNode) == 1) {
                if (proxy.getHasChilden() != 1) {
                    proxy.setHasChilden(1);
                    customProxyDao.updateProxyChildenNodeById(proxy);
                }
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean removeParentProxyChilden(String childCustomKey) {
        ChannelCustom custom = channelCustomDao.getCustomByCustomkey(childCustomKey,null);
        OrganizationNode childNode = customProxyDao.getProxyChildenNodeByCustomKey(childCustomKey,null);
        if (childNode == null) {
            channelCustomDao.removeProxyTypeById(custom.getId());
            channelCustomDao.removeAgentIdById(custom.getId());
            return true;
        }
        if (childNode.getParentId() != 0) {
            int oldParentId = childNode.getParentId();
            if (childNode.getHasChilden() == -1) {
                //没有子节点,直接删除当前节点
                customProxyDao.removeProxyChildenById(childNode.getId());

            } else {
                //有子节点，更新levelCode.
                int oldContentLevel = childNode.getContentLevel();
                String oldLevelCode = childNode.getLevelCode();
                List<OrganizationNode> nodes = customProxyDao.queryProxyChildenChild(childNode.getLevelCode(), childNode.getId());
                childNode.setLevelCode(childNode.getId() + "-");
                childNode.setContentLevel(1);
                childNode.setParentId(0);
                if (customProxyDao.updateProxyChildenNodeById(childNode) == 1) {
                    if (nodes != null && nodes.size() > 0) {
                        for (OrganizationNode node : nodes) {
                            node.setLevelCode(node.getLevelCode().replaceAll(oldLevelCode, childNode.getLevelCode()));
                            node.setContentLevel(node.getContentLevel() + (childNode.getContentLevel() - oldContentLevel));
                            customProxyDao.updateProxyChildenNodeById(node);
                        }
                    }
                }
            }

            OrganizationNode parentNode = customProxyDao.getProxyChildenNodeById(oldParentId);
            List<OrganizationNode> organizationNodes = customProxyDao.queryProxyChildenChild(parentNode.getLevelCode(), parentNode.getId());
            if (organizationNodes.isEmpty() && organizationNodes.size() == 0) {
                parentNode.setHasChilden(-1);
                customProxyDao.updateProxyChildenNodeById(parentNode);

                //被删除的代理商的父代理商没有任何子代理商，并且父代理商没有父代理商,更新关联代理商标识字段
                if (parentNode.getParentId() == 0) {
                    ChannelCustom parentCustom = channelCustomDao.getCustomByCustomkey(parentNode.getCustomKey(),null);
                    channelCustomDao.removeProxyTypeById(parentCustom.getId());
                }
            }

            if (custom != null) {
                channelCustomDao.removeAgentIdById(custom.getId());
            }
        }
        return true;
    }


    /**
     * 根据顶级customKey查询下面所有代理商、集团商户、商户customKey
     *
     * @param customKey
     * @return
     */
    @Override
    public List<String> queryAllCurrentAndChildrenCustomkeys(String customKey) {
        List<String> customKeys = new ArrayList<>();
        //查询本节点的所有代理商节点
        List<String> proxyCustomKeyList = customProxyDao.queryProxyChildenCustomKeyCurrentAndChildren(customKey);
        //把父节点的代理商添加进去
        proxyCustomKeyList.add(customKey);

        if (proxyCustomKeyList != null) {
            //循环代理商节点
            for (String proxyCustomKey : proxyCustomKeyList) {
                OrganizationNode node = customProxyDao.getNodeByCustomKey(proxyCustomKey,null);
                if (node != null && node.getCustomType() == 3) {
                    //查询代理商下面所有商户
                    List<String> groupCustomkeyList = customProxyDao.queryProxyCurrentAndChildrenCustomkeys(node.getLevelCode());
                    if (groupCustomkeyList != null) {
                        customKeys.addAll(groupCustomkeyList);
                        //循环商户,查看下面有没有集团性商户
                        for (String groupCustomkey : groupCustomkeyList) {
                            OrganizationNode groupNode = customGroupDao.getNodeByCustomKey(groupCustomkey,null);
                            if (groupNode != null && groupNode.getCustomType() == 5) {
                                List<String> groupCustomKeyList = customGroupDao.queryGroupCurrentAndChildrenCustomkeys(groupNode.getLevelCode());
                                if (groupCustomKeyList != null) {
                                    customKeys.addAll(groupCustomKeyList);
//                            customKeys.addAll(queryGroupChildrenCustomkeys(groupCustomkey));
                                }
                            }
                        }
                    }
                }
            }
        }

        return customKeys;
    }

    private List<String> queryCusotmKeysFromProxyChilden(String queryMode, int nodeId) {
        OrganizationNode nodeById = customProxyDao.getProxyChildenNodeById(nodeId);
        OrganizationNode proxyNode = customProxyDao.getNodeByCustomKey(nodeById.getCustomKey(),null);
        List<String> customKeys = new ArrayList<>();
        switch (queryMode) {
            case QueryType.QUERY_CURRENT_AND_CHILDREN:
                customKeys = customProxyDao.queryProxyChildenCurrentAndChildrenCustomkeys(nodeById.getLevelCode());
                if (proxyNode != null) {
                    customKeys.addAll(customProxyDao.queryProxyCurrentAndChildrenCustomkeys(proxyNode.getLevelCode()));
                }
                break;
            case QueryType.QUERY_CURRENT:
                customKeys.add(nodeById.getCustomKey());
                break;
            default:
        }
        return customKeys;
    }


	@Override
	public List<String> queryNodeCusotmKey(String customKey, int customType,
			String queryMode, int nodeId) {
        List<String> customKeys = null;
        switch (customType) {
            case QueryType.CUSTOM:
                customKeys = queryCusotmKeysFromCustom(customKey,nodeId);
                break;
            case QueryType.CUSTOM_GROUP:
                customKeys = queryCusotmKeysFromGourp(queryMode, nodeId);
                break;
            case QueryType.CUSTOM_PROXY:
                customKeys = queryCusotmKeysFromProxy(queryMode, nodeId);
                break;
            case QueryType.COMPANY:
                customKeys = queryCusotmKeysFromCompany(queryMode, nodeId);
                break;
            case QueryType.CUSTOM_PROXY_CHILDEN:
                customKeys = queryCusotmKeysFromProxyChilden(queryMode, nodeId);
                break;
            default:
        }
        return customKeys;
	}


	private List<String> queryCusotmKeysFromCustom(String customkey, int nodeId) {
        List<String> customKeys = new ArrayList<>();
        OrganizationNode node = new OrganizationNode();
        node = customProxyDao.getNodeByIdCustomkey(nodeId,customkey);
        if(node==null){
        	node= customGroupDao.getNodeByIdCustomkey(nodeId,customkey);
        }
        if(node==null){
            ChannelCustom customById = channelCustomDao.getCustomById(nodeId);
            customKeys.add(customById.getCustomkey());
        }else{
        	 customKeys.add(node.getCustomKey());
        }
        return customKeys;
	}
}
