# Payment received view

apk link: [click here](https://github.com/ys-pro-duction/UNotifyer/releases/download/1.1/app-release.apk).

<img src="screenshot/payment_view.jpeg" width="50%">

# Flow of app

<div>
  <img src="screenshot/s10.png" width="30%">
  <img src="screenshot/s8.png" width="30%">
  <img src="screenshot/s9.png" width="30%">
</div>

##### As sender

1. Enter receiver mobile number
2. Now a notification listner activated
3. when get **PhonePe Business** notification
4. read notification
5. if it's payment notification
6. send **SMS** to receiver mobile number

##### As receiver

1. Enter sender mobile number
2. Now a **SMS** listener activated
3. when get a **SMS**
4. if it's from sender number
5. it read payment info and display to lockscreen and show notification of payment

# Install Troubleshooting

* Some time play protect block this app to being install because it's not available on play store.
* here how to fix

<div>
  <img src="screenshot/s1.jpeg" width="15%">
  <img src="screenshot/s2.jpeg" width="15%">
  <img src="screenshot/s3.jpeg" width="15%">
  <img src="screenshot/s4.jpeg" width="15%">
  <img src="screenshot/s5.jpeg" width="15%">
</div>

and in some devices you are unable to enable notification access permission
just go to app info and click three on top right corner and allow restricted permission and restart.

### In Xiaomi/Redmi/Mi mobile
in xiaomi device you need to enable to show notification on lock screen
* Click and hold app icon
* click app info
* click notification and enable **Lock screen notifications**
* now click Other permissions and enable **Show on lock screen**
