package pe.edu.upeu.reserva.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.upeu.reserva.filter.CorrelationIdFilter;

@Configuration
public class FeignTraceConfig {

    @Bean
    public RequestInterceptor traceIdInterceptor() {
        return template -> {
            String traceId = MDC.get(CorrelationIdFilter.MDC_KEY);
            if (traceId != null) {
                template.header(CorrelationIdFilter.TRACE_ID_HEADER, traceId);
            }
        };
    }
}
