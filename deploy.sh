sbt example/fullLinkJS
yarn exec -- vite build
cp dist/index.html dist/200.html
surge ./dist 'animus-examples.surge.sh'