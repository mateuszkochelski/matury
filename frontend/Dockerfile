FROM oven/bun:1 AS base
WORKDIR /app


FROM base AS install

COPY package.json bun.lock . 

RUN bun install --frozen-lockfile

copy . .

RUN chown -R bun:bun /app

USER bun
EXPOSE 3000/tcp
ENTRYPOINT [ "bun", "dev"]
