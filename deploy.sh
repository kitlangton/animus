sbt example/fullLinkJS
npm run build:prod
cp dist/index.html dist/200.html
npx surge ./dist 'animus-docs.surge.sh'

