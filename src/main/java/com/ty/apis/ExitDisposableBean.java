package com.ty.apis;

import com.ty.apis.timer.NetEasyClawer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class ExitDisposableBean implements DisposableBean, ExitCodeGenerator {

    private static final Logger logger = LogManager.getLogger(ExitDisposableBean.class);

    @Override
    public void destroy() throws Exception {

        NetEasyClawer netEasyClawer = NetEasyClawer.getInstance();
        netEasyClawer.StopTimer();
        logger.info("<<<<<<<<<<<我被销毁了>>>>>>>>>>>>>>>");
    }

    @Override
    public int getExitCode() {

        logger.info("<<<<<<<<<<<getExitCode.>>>>>>>>>>>>>>>");

        return 0;
    }
}