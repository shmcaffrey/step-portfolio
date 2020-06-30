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


let myImg = document.getElementById("yoga-poses");
let yogaReveal = document.getElementById("yoga-link");
let numClick = -1;
var imgArray = new Array();

imgArray[0] = new Image();
imgArray[0].src = "images/Yogi/yogi.png";

imgArray[1] = new Image();
imgArray[1].src = "images/Yogi/yogi1.png";

imgArray[2] = new Image();
imgArray[2].src = "images/Yogi/yogi2.png";

imgArray[3] = new Image();
imgArray[3].src = "images/Yogi/yogi3.png";

imgArray[4] = new Image();
imgArray[4].src = "images/Yogi/yogi4.png";

imgArray[5] = new Image();
imgArray[5].src = "images/Yogi/yogi5.png";

imgArray[6] = new Image();
imgArray[6].src = "images/Yogi/yogi6.png";

imgArray[7] = new Image();
imgArray[7].src = "images/Yogi/yogi7.png";

imgArray[8] = new Image();
imgArray[8].src = "images/Yogi/yogi8.png";

var sanArr = ["Adho Mukha Svansana", "Padmasana", "Virabhadrasana II", "Kapotasana", "Virabhadrasana III",
              "Urdhva Mukha Paschimottanasana", "Vriksasana", "Halasana", "Urdhva Mukha Shvanasana"];

var engArr = ["Downward Facing Dog", "Lotus Pose", "Warrior Two", "Pigeon Pose", "Warrior three",
  "Upward-facing Intense Stretch Pose", "Tree Pose", "Plow Pose", "Upward Facing Dog"];

yogaReveal.onclick = () => {
  numClick++;
  let index = numClick % 8;
  myImg.src = imgArray[index].src;
  if (numClick === 0) {
    document.getElementById("san-text").innerText = "You found it! Keep clicking for more.";
  }
  else {
    document.getElementById("san-text").innerText = sanArr[index];
    document.getElementById("eng-text").innerText = engArr[index];
  }

}


// To parse JSON into a usable object for javascript
function getComments() {

    // fetch data from server, parse as json file, then reference coms as an object
    fetch('/data').then(response => response.json()).then((comments) => {
        const commentList = document.getElementById("comment-container");
        commentList.innerText = '';
        console.log("fetching comments");
        for (i = 0; i < comments.length; i++) {
          commentList.appendChild(createListElement(comments[i]));
        }
    });
}

// For learning promises
function getCommentsLong() {
    console.log('fetching JSON');

    const responsePromise = fetch('/data');

    responsePromise.then(handleResponse);
}

function handleResponse(response) {
    console.log('convert JSON promise stream to text');
    const textPromise = response.json();
    console.log(textPromise);
    textPromise.then(addListToDom);
}

function addListToDom(list) {
    const commentList = document.getElementById("comment-container");
    commentList.innerText = '';
    // list.pageOne does not work as a JSON element should, should this work with array format?
    commentList.appendChild(
      createListElement("Page One: " + list[0]));
    commentList.appendChild(
      createListElement("Page Two: " + list[1]));
    commentList.appendChild(
      createListElement("Page Three: " + list[2]));
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

async function getCommentsAsyncAwait() {
    const response = await fetch('/data');
    const comments = await response.json();
    const commentList = document.getElementById("comment-container");
    commentList.innerText = '';
    commentList.appendChild(
      createListElement("Page One: " + comments[0]));
    commentList.appendChild(
      createListElement("Page Two: " + comments[1]));
    commentList.appendChild(
      createListElement("Page Three: " + comments[2]));

}