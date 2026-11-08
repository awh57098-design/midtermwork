from pathlib import Path
import math,random,struct,wave
ROOT=Path(__file__).resolve().parents[1]
RATE=44100
DURATIONS={'hit':.12,'counter':.24,'guarded':.14,'critical':.27,'immune':.16,'hurt':.28,'boss_warning':.42}
for idx,(name,duration) in enumerate(DURATIONS.items()):
 rng=random.Random(730+idx);samples=[];phase=0;low=0
 for n in range(int(RATE*duration)):
  t=n/RATE;u=t/duration;noise=rng.uniform(-1,1);low=.82*low+.18*noise
  if name=='hit':v=(.55*math.sin(2*math.pi*115*t)+.45*noise)*math.exp(-t*30)+.2*math.sin(2*math.pi*1450*t)*math.exp(-t*55)
  elif name=='counter':phase+=2*math.pi*(500+u*1000)/RATE;v=(.65*math.sin(phase)+.25*math.sin(phase*1.5))*math.exp(-t*11)+noise*.12*math.exp(-t*45)
  elif name=='guarded':v=(low*.8+math.sin(2*math.pi*175*t)*.4)*math.exp(-t*32)
  elif name=='critical':freq=(1000,1450,1900)[min(2,int(u*3))];phase+=2*math.pi*freq/RATE;v=(math.sin(phase)*.6+math.sin(phase*2)*.18)*(1-u)**1.4
  elif name=='immune':v=math.sin(2*math.pi*440*t)*math.sin(math.pi*u)**2*.5
  elif name=='hurt':phase+=2*math.pi*(350-260*u)/RATE;v=(math.sin(phase)+.18*noise)*(1-u)**2
  else:
   start,end,freq=(0,.14,780) if t<.19 else (.19,.37,1040);a=(t-start)/(end-start);v=math.sin(2*math.pi*freq*t)*math.sin(math.pi*a)**2 if 0<=a<=1 else 0
  samples.append(v*min(1,t/.003)*min(1,(duration-t)/.008))
 peak=max(abs(v) for v in samples) or 1
 out=ROOT/'assets/audio'/f'{name}.wav';out.parent.mkdir(parents=True,exist_ok=True)
 with wave.open(str(out),'wb') as w:w.setparams((1,2,RATE,0,'NONE','not compressed'));w.writeframes(b''.join(struct.pack('<h',round(v/peak*.65*32767)) for v in samples))
 print(name,len(samples),'samples')
