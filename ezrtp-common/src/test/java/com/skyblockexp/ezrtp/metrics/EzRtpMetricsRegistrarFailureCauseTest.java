package com.skyblockexp.ezrtp.metrics;

import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EzRtpMetricsRegistrarFailureCauseTest {

    @Test
    void describeTopFailureCause_prefersEconomyBucketWhenEconomyFailuresLead() throws Exception {
        RtpStatistics statistics = new RtpStatistics();
        statistics.recordEconomyFailure();
        statistics.recordEconomyFailure();
        statistics.recordTeleportApiFailure();

        EzRtpMetricsRegistrar registrar = createRegistrar(statistics);

        assertEquals("Economy", invokeDescribeTopFailureCause(registrar));
    }

    @Test
    void describeTopFailureCause_reportsOfflineCancelledBucket() throws Exception {
        RtpStatistics statistics = new RtpStatistics();
        statistics.recordPlayerOfflineOrCancelledFailure();

        EzRtpMetricsRegistrar registrar = createRegistrar(statistics);

        assertEquals("Offline/Cancelled", invokeDescribeTopFailureCause(registrar));
    }

    @Test
    void describeTopFailureCause_reportsTeleportApiBucket() throws Exception {
        RtpStatistics statistics = new RtpStatistics();
        statistics.recordTeleportApiFailure();
        statistics.recordTeleportApiFailure();

        EzRtpMetricsRegistrar registrar = createRegistrar(statistics);

        assertEquals("Teleport API", invokeDescribeTopFailureCause(registrar));
    }

    @Test
    void describeTopFailureCause_reportsSearchErrorBucket() throws Exception {
        RtpStatistics statistics = new RtpStatistics();
        statistics.recordGenericSearchErrorFailure();

        EzRtpMetricsRegistrar registrar = createRegistrar(statistics);

        assertEquals("Search Error", invokeDescribeTopFailureCause(registrar));
    }

    private EzRtpMetricsRegistrar createRegistrar(RtpStatistics statistics) {
        RandomTeleportService service = mock(RandomTeleportService.class);
        when(service.getStatistics()).thenReturn(statistics);
        return new EzRtpMetricsRegistrar(null, () -> service);
    }

    private String invokeDescribeTopFailureCause(EzRtpMetricsRegistrar registrar) throws Exception {
        Method method = EzRtpMetricsRegistrar.class.getDeclaredMethod("describeTopFailureCause");
        method.setAccessible(true);
        return (String) method.invoke(registrar);
    }
}
