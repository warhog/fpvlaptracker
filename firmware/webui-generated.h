#pragma once
namespace comm {
class WifiWebServerFiles {

public:
constexpr static char const *header = R"(
<html>
<head>
    <style>
        body {
            font-family: Arial;
            background-color: #ffffff;
            color: #333333;
        }
        a {
            color: #ae93d0;
        }
        #content {
            background-color: #fcfcfc;
            margin: auto;
            border: 1px solid #357dbc;
            padding: 20px;
            width: 600px;
        } 
        #overlay {
            position: fixed;
            display: none;
            width: 100%;
            height: 100%;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-color: rgba(0,0,0,0.5);
            z-index: 2;
            cursor:wait;
        }
        #overlaydata {
            width: 500px;
            padding: 20px;
            border: 1px solid #357dbc;
            background-color: #fcfcfc;
            position: absolute;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
        }
        h1,h2,h3 {
            color: #ae93d0;
        }
        .button {
            font-size: 1.0em;
            border: 1px solid #357dbc;
            background-color: #428aca;
            color: #fcfcfc;
            text-decoration: none;
            display: inline-block;
            margin: 5px;
            padding: 8px;
         }
        .button-danger {
            border: 1px solid #e43725;
            background-color: #e74c3c;
        }
    </style>
    <title>fpvlaptracker node</title>
</head>
<body>
<div id='overlay'>
    <div id='overlaydata'>
        <div id='progressbar'>
            firmware upload progress:<br />
            <progress id='progress' style='margin-top: 10px; width: 90%;'></progress> <span id='percent'></span>
        </div>
        <div id='rebooting' style='display: none;'>
            rebooting node...please wait...
        </div>
    </div>
</div>
<div id='content'>
    <h1>fpvlaptracker32</h1>
    chip id: %CHIPID%<br />
    current version: %VERSION%<br />
    build date: %DATETIME% %COMMIT%<br />
    <hr size='1'/>
    <br />    )";

constexpr static char const *footer = R"(
</div>
<script>
    function overlay() {
        document.getElementById('overlay').style.display = 'block';
    }
    function rebooting() {
        overlay();
        document.getElementById('rebooting').style.display = 'block';
        document.getElementById('progressbar').style.display = 'none';
        window.setTimeout(function() {
            window.location.href = '/';
        }, 15000);
    }
    if (document.getElementById('upload_form') !== null) {
        document.getElementById('upload_form').onsubmit = function (evt) {
            evt.preventDefault();
            overlay();
            let form = document.getElementById('upload_form');
            let data = new FormData(form);
            let xhr = new XMLHttpRequest();
            let progress = document.getElementById('progress');
            let percent = document.getElementById('percent');
            progress.value = 0;
            progress.max = 100;
            percent.innerHTML = '0 %';
            xhr.upload.addEventListener('progress', function(evt) {
                if (evt.lengthComputable) {
                    let per = Math.round(evt.loaded / evt.total * 100);
                    progress.value = per;
                    percent.innerHTML = (per < 10 ? '0' : '') + per + ' %';
                }
            }, false);
            xhr.upload.addEventListener('error', function(evt) {
                alert('error during upload!\n\nplease reset device manually!');
                console.log('error during upload', evt);
            }, false);
            xhr.upload.addEventListener('load', function(evt) {
                rebooting();
        }, false);
            xhr.open('POST', '/update');
            xhr.send(data);
        }
    }

    function uploadChange() {
        let file = document.getElementById('update').files[0];
        if (!file) {
            console.log('no file selected');
            return;
        }

        if (file.name.split('.').pop() != 'bin') {
            alert('no .bin file selected. please select a .bin file.');
        }
    }
</script>
</body>
</html>
)";

constexpr static char const *index = R"(
rssi: %RSSI%<br />
<br />
<a class='button' href='/bluetooth'>switch to bluetooth</a> <a class='button' href='/reset'>restart node</a><br />
<h2>maintenance</h2>

<h3>firmware update</h3>
select .bin file to flash and press update to start the over the air update.<br />
<b>attention:</b> do not interrupt power supply, else you might need to reflash using the serial adapter!<br />
<form method='POST' action='#' enctype='multipart/form-data' id='upload_form'>
    <input type='file' id='update' name='update' onchange='uploadChange();'/>
    <input type='submit' class='button' value='update' />
</form>

<h3>voltage reference</h3>
calibration of the voltage readings<br />
<a class='button' href='/vref'>output voltage reference</a><br />

<h3>wifi settings</h3>
<a class='button' href='/wifi'>wifi settings</a><br />

<h3>factory defaults</h3>
<b>attention:</b> all data is reset to factory defaults except voltage reference<br />
<a class='button button-danger' href='/factorydefaults'>restore factory defaults</a>
)";

constexpr static char const *wifi = R"(
<script>
function setWifi(wifi) {
    document.getElementById('ssid').value = wifi;
    document.getElementById('password').value = '';
}
</script>
<a class='button' href='/'>back</a>
<br />
<h2>wifi</h2>
<form method='POST' action='/setwifi' enctype='multipart/form-data'>
    ssid: <input type="text" id="ssid" name="ssid" value="%SSID%" /><br />
    password: <input type="password" id="password" name="password" value="%PASSWORD%" /><br />
    <input type='submit' class='button' value='set wifi' />
</form>

<h3>available wifi networks</h3>
<ul>
    %WIFIS%
</ul>
)";

};
}
