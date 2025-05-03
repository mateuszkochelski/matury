FROM oven/bun:1 AS base
WORKDIR /app


FROM base AS install

COPY package.json bun.lock .
COPY patches ./patches

RUN bun i --frozen-lockfile

copy . .

RUN chown -R bun:bun /app

USER bun
EXPOSE 3000/tcp
ENTRYPOINT [ "bun", "dev"]
