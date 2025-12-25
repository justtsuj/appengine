package com.rokid.rkengine.confirm;

import com.rokid.rkengine.utils.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class ReporterManager {
    private static final int BLOCKING_QUEUE_CAPACITY = 30;
    private static final int POOL_CORE_SIZE = 1;
    private static final long POOL_KEEP_TIME = 30;
    private static final int POOL_MAX_SIZE = 10;
    ExecutorService threadPoolExecutor;

    private ReporterManager() {
        this.threadPoolExecutor = new ThreadPoolExecutor(1, 10, POOL_KEEP_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue(BLOCKING_QUEUE_CAPACITY));
    }

    public void executeReporter(ConfirmReporter confirmReporter) {
        if (confirmReporter != null) {
            Logger.m0d("executeReporter ");
            this.threadPoolExecutor.execute(confirmReporter);
        }
    }

    public static ReporterManager getInstance() {
        return SingleHolder.instance;
    }

    private static class SingleHolder {
        private static final ReporterManager instance = new ReporterManager();

        private SingleHolder() {
        }
    }
}
