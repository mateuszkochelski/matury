FROM oven/bun:1 AS base
WORKDIR /app

FROM base AS install

ARG NEXT_PUBLIC_BACKEND_URL="http://backend:8080"
ARG BACKEND_URL="http://backend:8080"

ENV NEXT_PUBLIC_BACKEND_URL=${NEXT_PUBLIC_BACKEND_URL}
ENV BACKEND_URL=${BACKEND_URL}

COPY package.json bun.lock .

RUN if [ -d patches ]; then cp -r patches ./patches; fi

RUN bun i --frozen-lockfile

COPY . .

RUN chown -R bun:bun /app

USER bun
RUN bunx next build

FROM node:18-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production

ARG NEXT_PUBLIC_BACKEND_URL="http://backend:8080"
ARG BACKEND_URL="http://backend:8080"
ENV NEXT_PUBLIC_BACKEND_URL=${NEXT_PUBLIC_BACKEND_URL}
ENV BACKEND_URL=${BACKEND_URL}

COPY --from=install /app /app

EXPOSE 3000

CMD ["node", "node_modules/next/dist/bin/next", "start"]
