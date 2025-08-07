FROM oven/bun:1 AS base
WORKDIR /app


FROM base AS install

COPY package.json bun.lock .
COPY patches ./patches

ARG NEXT_PUBLIC_BACKEND_URL
ARG BACKEND_URL

ENV NEXT_PUBLIC_BACKEND_URL="http://backend:8080"
ENV BACKEND_URL="http://backend:8080"

RUN NEXT_PUBLIC_BACKEND_URL=$NEXT_PUBLIC_BACKEND_URL \
    BACKEND_URL=$BACKEND_URL \
    bun i --frozen-lockfile

# Skopiuj pozostałe pliki aplikacji
COPY . .

# Napraw uprawnienia
RUN chown -R bun:bun /app

# Budowanie produkcyjne jako użytkownik bun
USER bun
RUN bunx next build

# Finalny etap uruchamiania
FROM base AS runner
WORKDIR /app

ENV NODE_ENV=production

# Skopiuj zbudowaną aplikację z poprzedniego etapu
COPY --from=install /app /app

# Napraw uprawnienia jeszcze raz (jeśli warstwa COPY zmieniła właściciela)
RUN chown -R bun:bun /app

USER bun

EXPOSE 3000

# Produkcyjne uruchomienie aplikacji Next.js
CMD ["bunx", "next", "start"]
