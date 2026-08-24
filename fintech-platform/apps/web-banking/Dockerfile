FROM node:22-alpine AS build
WORKDIR /build

# Vite inlines VITE_* env vars into the built JS at build time, not at
# container start — so unlike the Java services' DB_URL etc., this can't be
# overridden with `docker-compose`'s `environment:` on the running
# container. It has to be a build arg instead. The default matches
# docker-compose's setup: the browser (on the host) reaches the gateway via
# its published port, so localhost:8080 is correct even though this
# container never talks to the gateway itself.
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

COPY package.json package-lock.json* ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
