package com.jrmf.service;

import com.jrmf.domain.TaskBaseConfig;
import com.jrmf.persistence.TaskBaseConfigDao;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class TaskBaseConfigServiceImpl implements TaskBaseConfigService {

    @Value("${os.id}")
    private String osId;

    @Autowired
    private TaskBaseConfigDao taskBaseConfigDao;
    @Autowired
    private UtilCacheManager cacheManager;

    @Override
    public TaskBaseConfig getConfigByOsId() {
        TaskBaseConfig taskBaseConfig = (TaskBaseConfig) cacheManager.get(osId);
        if (taskBaseConfig == null) {
            taskBaseConfig = taskBaseConfigDao.getConfigByOsId(osId);
            cacheManager.put(osId, taskBaseConfig, -1);
        }

        return taskBaseConfig;
    }
}
 