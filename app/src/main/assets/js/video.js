
window.onload = function () {
    if (document.getElementById('container') === null || document.getElementById('container') === undefined) {
        console.log('do not have video');
        return;
    }

    var video = document.getElementById('mov');
    var cH = document.getElementById('container').clientHeight;
    var vH = video.clientHeight;
    //console.log(document.getElementById('container').clientHeight);
    //console.log(document.getElementById('mov').clientHeight);
    //console.log((cH-vH)/2);
    video.style.marginTop = (cH-vH)/2 + 'px';
    var url = location.href;
    var srcUrl = url.slice(url.indexOf('?') + 1, url.length);
    if (srcUrl.indexOf('src=') < 0) {
        alert('src error');
        return;
    }
    srcUrl = srcUrl.slice(srcUrl.indexOf('=')+1, srcUrl.length);

    var source = video.getElementsByTagName('source');    

    //console.log(source);
    source[0].src = srcUrl;
    video.load();
    video.addEventListener('loadeddata', function(){
        video.style.visibility = 'visible';
    });
}


function qnaNotice(src)
{
    if (src === '' || src === null || src === undefined) {
        alert('src is null');
        return;
    }
    location.assign("video.html?src="+src);
}