package pe.edu.upeu.reserva.config;
@org.springframework.context.annotation.Configuration
public class InternalFeignConfig {
 @org.springframework.context.annotation.Bean
 public feign.RequestInterceptor internal(@org.springframework.beans.factory.annotation.Value("${casa.internal.secret:casa-local-internal-change-me}") String secret){return template->template.header("X-Internal-Secret",secret);}
}
