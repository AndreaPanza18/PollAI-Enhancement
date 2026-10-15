# PollAI-Enhancement

Progetto full-stack Spring Boot per la gestione di un pollaio, con un modulo
web, un server Eureka e i componenti di intelligenza artificiale.

## Struttura

- `webapp/`: applicazione principale Spring Boot.
- `modulo-AI/EurekaServer/`: server Eureka per il service discovery.
- `modulo-AI/Notebook/`: notebook e dataset per il modello di produzione delle uova.
- `modulo-AI/UovaService/`: API Flask che espone il modello di predizione.

## Build multi-modulo

Il progetto è un reactor Maven multi-modulo. Il wrapper Maven e la
configurazione condivisa sono nella root:

```powershell
.\mvnw.cmd clean verify
```

Per compilare un solo modulo:

```powershell
.\mvnw.cmd -pl webapp -am verify
.\mvnw.cmd -pl modulo-AI/EurekaServer -am verify
```

Su Linux/macOS usare `./mvnw` al posto di `.\mvnw.cmd`.

## Benchmark JMH

I benchmark JMH del modulo `webapp` sono disponibili tramite il profilo
dedicato:

```powershell
.\mvnw.cmd -pl webapp -Pjmh test-compile exec:java "-Dexec.args=MagazzinoBenchmark -wi 3 -i 5 -f 1"
```

Le dipendenze JMH restano limitate al classpath di test e non vengono incluse
nel WAR.
