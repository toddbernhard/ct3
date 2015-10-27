import http from 'http'
import https from 'https'
import httpProxy from 'http-proxy'
import config from 'config'
import express from 'express'
import fs from 'fs'
import cookieParser from 'cookie-parser'
import {randomInt} from '../lib/util'

var options = {
  // key: fs.readFileSync('config/cert.key'),
  // cert: fs.readFileSync('config/cert.crt')
};

var app = express();
app.use(cookieParser())

var servers = config.get('servers');

console.log("SERVERS: ", servers)
var serverMap = {}
for (let s of servers) {
    console.log("SERVER: ", s)
    serverMap[s.name] = s
}
//
// Create a proxy server with custom application logic
//
var proxy = httpProxy.createProxyServer({})

proxy.on('error', function (error, req, res) {
    console.log('proxy error', error);
    res.end("DUDE");
});

app.all('/*', function(req, res, next) {
  let target = serverMap[req.cookies.LB_STICKY] || servers[randomInt(0, servers.length)]
  console.log(`COOKIE: ${req.cookies.LB_STICKY} - ${req.path} --> ${target.host}`)
  res.cookie('LB_STICKY', target.name);
  proxy.web(req, res, {
    target: target.host,
    secure: false,
    changeOrigin: true,
    autoRewrite: true
  })
})

console.log("listening at http://localhost:" + config.get('http-port'))
http.createServer(app).listen(config.get('http-port'))

// console.log("listening at https://localhost:" + config.get('https-port'))
// https.createServer(options, app).listen(config.get('https-port'))
