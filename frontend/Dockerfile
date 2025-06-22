FROM oven/bun:1 AS base
WORKDIR /app

FROM base AS install

COPY package.json bun.lock .
COPY patches ./patches

ARG NEXT_PUBLIC_BACKEND_URL
ARG BACKEND_URL

ENV NEXT_PUBLIC_BACKEND_URL="http://backend:8080"
ENV BACKEND_URL="http://backend:8080"

# Instalacja zależności
RUN NEXT_PUBLIC_BACKEND_URL=$NEXT_PUBLIC_BACKEND_URL \
    BACKEND_URL=$BACKEND_URL \
    bun i --frozen-lockfile

COPY . .

RUN chown -R bun:bun /app

# Tu robimy build Next.js przez next build
RUN bunx next build

FROM base AS prod
WORKDIR /app

COPY --from=install /app /app

USER bun
EXPOSE 3000/tcp
ENTRYPOINT ["bun", "start"]
