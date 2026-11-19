"""Verifica balanceo, perfiles y targets de Prometheus en el compose raíz."""
import verificar as v
import json,pathlib
root=pathlib.Path(__file__).resolve().parent.parent/'evidencias'
def count(port):
    s,r,_=v.call('/actuator/metrics/http.server.requests',base='http://localhost:'+str(port))
    return next(m['value'] for m in r['measurements'] if m['statistic']=='COUNT') if s==200 else 0
before=[count(18102),count(18103)]
for i in range(20):assert v.call('/api/v1/propiedades')[0]==200
after=[count(18102),count(18103)]
result={'solicitudes_gateway':20,'antes':before,'despues':after,'ambas_instancias_reciben':all(a>b for a,b in zip(after,before))}
assert result['ambas_instancias_reciben']
(root/'balanceo.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
s,m,_=v.call('/api/v1/query?query=up',base='http://localhost:19090')
jobs={x['metric'].get('job'):x['value'][1] for x in m['data']['result']}
assert len(jobs)>=9 and all(x=='1' for x in jobs.values()),jobs
profiles={}
for profile in ['dev','prod']:
    s,c,_=v.call('/casa-propiedad-ms/'+profile,base='http://localhost:18888');assert s==200
    p=c['propertySources'][0]['source'];profiles[profile]={'perfil':c['profiles'],'sql_visible':p.get('spring.jpa.show-sql')}
assert profiles['dev']['sql_visible'] is True and profiles['prod']['sql_visible'] is False
(root/'infraestructura.json').write_text(json.dumps({'metricas':jobs,'configuracion':profiles},indent=2),encoding='utf-8')
print('PASS balanceo, perfiles dev/prod y nueve targets de Prometheus')
