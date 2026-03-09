# ---------- build stage: собираем фронтенд ----------
FROM node:18-alpine AS build
WORKDIR /app

# Устанавливаем зависимости
COPY package*.json ./
RUN npm ci --no-audit --fund=false

# Копируем исходники и собираем
COPY . .
# В Vite переменные VITE_* читаются на этапе сборки.
# В проде будем звать бэкенд через относительный путь /api
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

# ---------- run stage: раздаём статику через Nginx ----------
FROM nginx:alpine
# Наш конфиг заменит дефолтный
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Кладём собранные файлы (Vite по умолчанию кладёт в dist)
COPY --from=build /app/dist/ /usr/share/nginx/html/
EXPOSE 80

# Простой healthcheck (в nginx:alpine есть wget из BusyBox)
HEALTHCHECK CMD wget -qO- http://localhost/ || exit 1