
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

let myImg = document.getElementById("yoga-poses");
let numClick = -1;

function revealPose () {
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