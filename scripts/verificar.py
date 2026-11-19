"""Pruebas reales por Gateway. Crea datos de demostración; no borra datos existentes."""
import json, time, urllib.request, urllib.error, urllib.parse, datetime, pathlib, concurrent.futures, os
BASE=os.environ.get('CASA_API','http://localhost:18080')
KC=os.environ.get('CASA_KC','http://localhost:18180')
results=[]
def call(path,method='GET',body=None,token=None,base=BASE,headers=None):
    h=dict(headers or {})
    if token:h['Authorization']='Bearer '+token
    data=None
    if body is not None:
        data=json.dumps(body).encode();h['Content-Type']='application/json'
    req=urllib.request.Request(base+path,data=data,headers=h,method=method)
    try:
        with urllib.request.urlopen(req,timeout=30) as r:
            raw=r.read();return r.status,json.loads(raw) if raw else None,dict(r.headers)
    except urllib.error.HTTPError as e:
        raw=e.read()
        try:value=json.loads(raw)
        except Exception:value=raw.decode(errors='replace')[:300]
        return e.code,value,dict(e.headers)
def check(name,condition,detail=''):
    results.append({'prueba':name,'resultado':'PASS' if condition else 'FAIL','detalle':detail})
    print(('PASS' if condition else 'FAIL')+' '+name,flush=True)
    if not condition:raise AssertionError(name+': '+str(detail))
def login(user):
    data=urllib.parse.urlencode({'grant_type':'password','client_id':'casa-pruebas','username':user,'password':'Casa2026!'}).encode()
    with urllib.request.urlopen(urllib.request.Request(KC+'/realms/casa/protocol/openid-connect/token',data=data),timeout=30) as r:return json.load(r)['access_token']
def esperar(rid,token,state):
    for i in range(60):
        status,r,_=call('/api/v1/reservas/'+str(rid),token=token)
        if status==200 and r['estado']==state:return r
        time.sleep(2)
    raise AssertionError('Reserva '+str(rid)+' no llegó a '+state+': '+str(r))
def crear_propiedad(admin,nombre):
    status,p,_=call('/api/v1/propiedades','POST',{'nombre':nombre,'descripcion':'Dato creado por prueba automatizada','precio':1000,'activo':True,'stock':1,'tipoPropiedadId':1,'direccion':'Av. Pruebas 123','ciudad':'Lima','areaM2':100,'dormitorios':3},admin)
    check('Crear propiedad '+nombre,status==201,str(status));return p['id']
def reservar(pid,token,method='TARJETA',trace=None):
    return call('/api/v1/reservas','POST',{'metodoPago':method,'detalles':[{'idPropiedad':pid,'cantidad':1}]},token,headers={'X-Trace-ID':trace} if trace else None)
def main():
    print('Esperando servicios y registro Eureka...',flush=True)
    for attempt in range(120):
        try:
            checks=[call('/actuator/health',base='http://localhost:'+str(p))[0]==200 for p in [18080,18102,18103,18104,18105,18106]]
            if all(checks) and call('/api/v1/propiedades')[0]==200:
                login('cliente1')
                break
        except Exception:pass
        time.sleep(2)
    else:raise RuntimeError('Servicios no disponibles. Revisa docker compose -f compose.json logs')
    admin=login('admin');uno=login('cliente1');dos=login('cliente2')
    check('Login de tres usuarios reales',bool(admin and uno and dos))
    s,_,_=call('/api/v1/propiedades');check('Catálogo público',s==200)
    s,_,_=call('/api/v1/reservas');check('Reserva sin JWT rechazada',s==401,str(s))
    s,_,_=call('/api/v1/reservas',token=uno[:-8]+'INVALIDO');check('JWT manipulado rechazado',s==401,str(s))
    s,_,_=call('/api/v1/propiedades','POST',{},uno);check('Cliente no administra propiedades',s==403,str(s))
    s,_,_=call('/api/v1/propiedades/1/descontar-stock?cantidad=1','PATCH',token=uno);check('Gateway bloquea mutación interna',s==403,str(s))
    profile={'tipoDocumento':'DNI','documento':'12345678','nombre':'Cliente de prueba','correo':'prueba@casa.local','telefono':'999999999','direccion':'Lima'}
    s,_,_=call('/api/v1/clientes/me','PUT',profile,uno);check('Guardar perfil del JWT',s==200,str(s))
    s,other,_=call('/api/v1/clientes/me',token=dos);check('Perfil no se comparte con otro usuario',s==404 or other.get('documento')!='12345678',str(s))
    stamp=str(int(time.time()));trace='casa-e2e-'+stamp
    pid=crear_propiedad(admin,'Casa aprobada '+stamp)
    s,r,h=reservar(pid,uno,trace=trace);check('Reserva creada por Gateway',s==201,str(s));rid=r['id']
    check('Trace-ID conservado',h.get('X-Trace-ID',h.get('x-trace-id'))==trace,str(h))
    esperar(rid,uno,'PAGADA');check('Kafka procesa pago aprobado',True,'reserva='+str(rid))
    s,p,_=call('/api/v1/pagos/por-reserva/'+str(rid),token=uno);check('Dueño consulta su pago',s==200,str(s))
    s,_,_=call('/api/v1/reservas/'+str(rid),token=dos);check('Reserva ajena devuelve 404',s==404,str(s))
    s,_,_=call('/api/v1/pagos/por-reserva/'+str(rid),token=dos);check('Pago de reserva ajena devuelve 404',s==404,str(s))
    s,_,_=call('/api/v1/pagos/'+str(p['id']),token=dos);check('Pago ajeno por ID devuelve 404',s==404,str(s))
    s,rows,_=call('/api/v1/reservas',token=dos);check('Lista excluye reservas ajenas',s==200 and rid not in [x['id'] for x in rows])
    s,_,_=call('/api/v1/pagos',token=uno);check('Listado global de pagos solo administrador',s==403,str(s))
    pid2=crear_propiedad(admin,'Casa rechazada '+stamp)
    s,r,_=reservar(pid2,uno,'TARJETA_RECHAZADA');check('Reserva para pago rechazado',s==201,str(s))
    esperar(r['id'],uno,'CANCELADA');s,p,_=call('/api/v1/propiedades/'+str(pid2));check('SAGA libera disponibilidad tras rechazo',s==200 and p['stock']==1,str(p.get('stock')))
    pid3=crear_propiedad(admin,'Casa concurrente '+stamp)
    with concurrent.futures.ThreadPoolExecutor(2) as pool:rows=list(pool.map(lambda t:reservar(pid3,t),[uno,dos]))
    check('Dos usuarios no reservan la misma propiedad',sum(1 for s,r,_ in rows if s==201 and r['estado']=='PENDIENTE_PAGO')==1,str([(s,r.get('estado')) for s,r,_ in rows]))
    s,_,_=call('/api/v1/documentos/DNI/123','GET',token=uno);check('Valida longitud de DNI',s==400,str(s))
    for i in range(15):
        try:
            query=urllib.parse.urlencode({'query':'{job=~"casa.*"} |= "'+trace+'"','limit':100})
            s,logs,_=call('/loki/api/v1/query_range?'+query,base='http://localhost:13100')
            streams=logs.get('data',{}).get('result',[]) if isinstance(logs,dict) else []
            if len(streams)>=2:break
        except Exception:streams=[]
        time.sleep(2)
    check('Trace visible en Loki en varios servicios',len(streams)>=2,'streams='+str(len(streams)))
if __name__=='__main__':
    path=pathlib.Path(__file__).resolve().parent.parent/'evidencias';path.mkdir(exist_ok=True)
    try:main()
    finally:
        (path/'integracion.json').write_text(json.dumps({'fecha':datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=-5))).isoformat(),'pruebas':results},ensure_ascii=False,indent=2),encoding='utf-8')
