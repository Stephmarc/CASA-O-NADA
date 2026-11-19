"""Verifica el perfil opcional s07, sin mostrar el JWT ni contraseñas en evidencias."""
import verificar as v
import json, pathlib, time, base64
base='http://localhost:18107'
email='s07-'+str(int(time.time()))+'@casa.local'
s,r,_=v.call('/api/v1/auth/registro','POST',{'email':email,'password':'Prueba2026!'},base=base)
assert s==201,(s,r)
s,r,_=v.call('/api/v1/auth/login','POST',{'email':email,'password':'Prueba2026!'},base=base)
assert s==200,(s,r)
token=r['access_token']
def decode(part):return json.loads(base64.urlsafe_b64decode(part+'='*(-len(part)%4)))
header=decode(token.split('.')[0]);claims=decode(token.split('.')[1])
s,jwks,_=v.call('/.well-known/jwks.json',base=base)
assert header['alg']=='RS256' and 'CLIENTE' in claims['realm_access']['roles'] and s==200
assert any(k['kid']==header['kid'] for k in jwks['keys'])
result={'registro':201,'login':200,'algoritmo':'RS256','rol':'CLIENTE','issuer':claims['iss'],'jwks_publica':True}
(pathlib.Path(__file__).resolve().parent.parent/'evidencias/s07.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print('PASS registro, login, claims y JWKS de S07')
