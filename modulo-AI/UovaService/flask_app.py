from flask import Flask, request, jsonify
import pandas as pd
import py_eureka_client.eureka_client as eureka_client
import os
import socket

from uova_model import uova_model

# Indirizzo con cui il servizio si registra su Eureka.
# Prima veniva usato il nome del PC (es. DESKTOP-XXXX.unisa.it), che fuori dalla
# rete d'ateneo non e' risolvibile -> UnknownHostException nella webapp.
# In Docker il nome host viene impostato da Compose; in locale resta localhost.
rest_port = int(os.getenv("UOVA_SERVICE_PORT", "8050"))
instance_host = os.getenv("EUREKA_INSTANCE_HOST", "localhost")
eureka_server = os.getenv("EUREKA_SERVER_URL", "http://localhost:8761/eureka")
eureka_client.init(eureka_server=eureka_server,
                   app_name="uova-service",
                   instance_port=rest_port,
                   instance_host=instance_host,
                   instance_ip=socket.gethostbyname(instance_host))

app = Flask(__name__)
model = uova_model()

@app.route("/health", methods=["GET"])
def health():
    return {"status": "ok"}

@app.route("/predizione", methods=['POST'])
def hello():
    data = request.json
    df = pd.DataFrame(data)
    df = df[["eta", "razza", "mangime", "temperatura", "peso"]]
    print(df.head())
    print(data)
    print("Prediction data columns:", df.columns)
    response_value = model.predict(df)
    return jsonify({"prediction": response_value})

if __name__ == "__main__":
    app.run(host='0.0.0.0', port = rest_port)
