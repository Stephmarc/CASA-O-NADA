package pe.edu.upeu.propiedad;
@org.springframework.stereotype.Component
public class InternalFilter extends org.springframework.web.filter.OncePerRequestFilter {
 @org.springframework.beans.factory.annotation.Value("${casa.internal.secret:casa-local-internal-change-me}") private String secret;
 @Override protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest r,jakarta.servlet.http.HttpServletResponse s,jakarta.servlet.FilterChain c) throws java.io.IOException,jakarta.servlet.ServletException {
  String p=r.getRequestURI();
  if((p.endsWith("/descontar-stock")||p.endsWith("/restaurar-stock")) && !java.security.MessageDigest.isEqual(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8),java.util.Objects.toString(r.getHeader("X-Internal-Secret"),"").getBytes(java.nio.charset.StandardCharsets.UTF_8))){s.sendError(403);return;}
  c.doFilter(r,s);
 }
}
