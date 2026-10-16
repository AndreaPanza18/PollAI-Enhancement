from flask import Flask, request, jsonify
import pandas as pd
import py_eureka_client.eureka_client as eureka_client
import socket
import sklearn
import json

from uova_model import uova_model

# Indirizzo con cui il servizio si registra su Eureka.
# Prima veniva usato il nome del PC (es. DESKTOP-XXXX.unisa.it), che fuori dalla
# rete d'ateneo non e' risolvibile -> UnknownHostException nella webapp.
# Webapp, Eureka e Flask girano sullo stesso PC, quindi usiamo localhost.
rest_port = 8050
eureka_client.init(eureka_server="http://localhost:8761/eureka",
                   app_name="uova-service",
                   instance_port=rest_port,
                   instance_host="localhost",
                   instance_ip="127.0.0.1")

app = Flask(__name__)
model = uova_model()

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