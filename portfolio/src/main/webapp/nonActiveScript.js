// The following code was used for learning promises. It is currently not being used and can be removed in the future once it isn't needed.
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