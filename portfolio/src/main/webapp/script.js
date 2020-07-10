// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


var slideIndex = 1;
showSlides(slideIndex);

function plusSlides(n) {
  showSlides(slideIndex += n);
}

function showSlides(n) {
  var i;
  var slides = document.getElementsByClassName('mySlides');

  if (n > slides.length) {slideIndex = 1}    
  if (n < 1) {slideIndex = slides.length}
  for (i = 0; i < slides.length; i++) {
    if (i == slideIndex - 1) {
      slides[slideIndex-1].style.display = 'block';
    }
    else {
      slides[i].style.display = 'none';  
    }
  }
}
  
// To parse JSON into a usable object for javascript
function getComments(numComments) {
  
  if (numComments == null) {
    numComments == '5';
  }

  // fetch data from server, parse as json file, then reference coms as an object
  fetch('/data?num-comments=' + numComments).then(response => response.json()).then((comments) => {
    const commentList = document.getElementById('comment-container');
    commentList.innerText = '';
    console.log('fetching comments');
      comments.forEach((comment) => {
        if (comment == null) {
          console.log('no comment')
        }
        else {
          console.log(comment);
          commentList.appendChild(createListElement(comment));
        }
      })
  });

  fetch('/blob-upload').then((response) => {
      return response.text();
    }).then((imageUploadUrl) => {
      const messageForm = document.getElementById('image-form');
      messageForm.action = imageUploadUrl;
      messageForm.classList.remove('hidden');
    });

  fetch('/store').then((response) => response.json()).then((imageUploadUrls) => {
    const imgSlides = document.getElementById('slideshow');
    imageUploadUrls.forEach((imgUrl) => {
      imgSlides.prepend(createSlideElement(imgUrl));
    })
    showSlides(1);
    imgSlides.classList.remove('hidden');

  });
}
  
/** Creates an <li> element containing text. */
function createListElement(comment) {
  const liElement = document.createElement('li');
  liElement.innerText = comment;
  return liElement;
}

//Once data from datastore is deleted then remove text from page
function deleteAllComments() {
    const commentList = getElementById('comment-container');
    const request = new Request('/delete-data', {method: 'POST'});
    fetch(request).then(response => response.text()).then(getComments(0));
}

function fillNumComments() {
    return document.getElementById('num-comments').value;
}

function createSlideElement(imgUrlIn) {
    var slide = document.createElement('div');
    slide.setAttribute('class', 'mySlides');
    
    var image = document.createElement('img');
    image.setAttribute('src', imgUrlIn);
    image.setAttribute('class', 'slideshow-img');

    //TODO: add count of pictures

    slide.appendChild(image);
    return slide;
}

