FROM python:slim

WORKDIR /app

COPY requirements.txt ./
RUN pip install -r requirements.txt

COPY . .

COPY mqtt_config.txt /app/mqtt_config.txt

CMD ["python", "-u", "service.py"]