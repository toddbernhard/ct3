import ajax from 'xmlhttprequest';
import _ from './server.js';
import config from 'config';

describe('server', () => {

    var getRequest = function (cookie, successCallback) {

        var xhr = new ajax.XMLHttpRequest();
        var url = 'http://localhost:' + config.get('http-port') + '/';

        xhr.open('GET', url);
        xhr.setDisableHeaderCheck(true);

        xhr.setRequestHeader('cookie', cookie);

        xhr.onerror = function () {
          console.log(xhr.responseText);
        };
        xhr.onload = successCallback;

        return xhr;
    };

    it('should reverse proxy to yahoo', done => {
        setTimeout(function () {
        }, 0);

        var xhr = getRequest('LB_STICKY=first', function () {

          expect(xhr.getResponseHeader('server')).toEqual('ATS');
          expect(xhr.responseText).toContain('Yahoo en Español');
          done();
        });

        xhr.send();
    });

    it('should reverse proxy to google', done => {
        setTimeout(function () {
        }, 0);

        var xhr = getRequest('LB_STICKY=second', function () {

          expect(xhr.getResponseHeader('server')).toEqual('gws');
          expect(xhr.responseText).toContain('Google Search');
          done();
        });

        xhr.send();
    });
});
