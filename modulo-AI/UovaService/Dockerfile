FROM python:3.11-slim
WORKDIR /app

ENV PYTHONUNBUFFERED=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
    UOVA_SERVICE_PORT=8050

COPY requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY flask_app.py uova_model.py uova_model.pkl ./

EXPOSE 8050
CMD ["python", "flask_app.py"]
