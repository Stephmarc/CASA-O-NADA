import { describe, it, expect } from 'vitest';
import { generarCodeChallenge, generarCodeVerifier, generarState } from './pkce';
import { decodeJwtPayload } from './jwt';
describe('Autenticación', () => {
  it('cumple vector PKCE S256 de RFC 7636', async () => {
    expect(await generarCodeChallenge('dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk')).toBe(
      'E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM',
    );
  });
  it('genera verificadores únicos y URL-safe', () => {
    let a = generarCodeVerifier(),
      b = generarCodeVerifier();
    expect(a).not.toBe(b);
    expect(a).toMatch(/^[A-Za-z0-9_-]{43}$/);
    expect(generarState()).toMatch(/^[A-Za-z0-9_-]+$/);
  });
  it('no interpreta tokens malformados', () => {
    expect(decodeJwtPayload('invalido')).toBeNull();
    expect(decodeJwtPayload('a.%%%%.b')).toBeNull();
  });
});
