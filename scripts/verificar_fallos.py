"""Pruebas de fallos sobre el compose local. Detiene y restaura solo servicios de CASA O NADA."""
import verificar as v
import pathlib,subprocess,time,json,datetime
ROOT=pathlib.Path(__file__).resolve().parent.parent
def docker(*args,**kwargs):
    return subprocess.run(['docker','compose','-f',str(ROOT/'compose.json'),*args],check=True,capture_output=True,text=True,**kwargs)
results=[]
def check(name,ok):
    results.append({'prueba':name,'resultado':'PASS' if ok else 'FAIL'})
    print(('PASS ' if ok else 'FAIL ')+name,flush=True)
    assert ok,name
try:
    admin=v.login('admin');token=v.login('cliente1');stamp=str(int(time.time()))
    pid=v.crear_propiedad(admin,'Prueba broker '+stamp)
    docker('stop','kafka')
    try:
        s,r,_=v.reservar(pid,token);check('Reserva persistida con Kafka apagado',s==201 and r['estado']=='PENDIENTE_PAGO')
    finally:docker('start','kafka')
    v.esperar(r['id'],token,'PAGADA');check('Outbox recupera pago al volver Kafka',True)
    event={'tipoEvento':'reserva.creada','reservaId':r['id'],'idCliente':r['idCliente'],'total':r['total'],'metodoPago':'TARJETA','origen':'prueba-idempotencia','timestamp':int(time.time()*1000)}
    docker('exec','-T','kafka','/opt/kafka/bin/kafka-console-producer.sh','--bootstrap-server','localhost:9092','--topic','reserva-eventos',input=json.dumps(event)+'\n'+json.dumps(event)+'\n')
    time.sleep(5)
    s,pagos,_=v.call('/api/v1/pagos',token=admin);check('Dos duplicados no crean otro pago',s==200 and len([p for p in pagos if p['reservaId']==r['id']])==1)
    pid2=v.crear_propiedad(admin,'Prueba circuito '+stamp)
    docker('stop','propiedad','propiedad2')
    try:
        s,failed,_=v.reservar(pid2,token);check('Caída de propiedad no confirma ni cobra',s==201 and failed['estado']=='CARRITO' and failed['total'] is None)
    finally:docker('start','propiedad','propiedad2')
finally:
    # The broker and both instances must remain available even if an assertion fails.
    docker('start','kafka','propiedad','propiedad2')
    (ROOT/'evidencias/fallos.json').write_text(json.dumps({'fecha':datetime.datetime.now().isoformat(),'pruebas':results},ensure_ascii=False,indent=2),encoding='utf-8')
