FROM forthecommunity/flutter:3.50.0 AS build

WORKDIR /app

COPY pubspec.* ./
RUN flutter pub get

COPY . .

ARG API_BASE_URL=http://localhost:8060
RUN flutter build web --release \
    --dart-define=API_BASE_URL=${API_BASE_URL}

# Production stage
FROM nginx:alpine

COPY --from=build /app/build/web /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
