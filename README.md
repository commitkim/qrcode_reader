# qrcode_reader
2022.04.12
QR 코드앱 만들기 / 앱 등록하기 

- https://play.google.com/store/apps/details?id=krow.dev.qrcode 비슷하게
- qr 코드 인식 후 http, geo, tel 등의 uri 분석해서 적절하게 실행하는 기능 구현
- 로컬에 히스토리 저장하는 기능 구현
- 적절한 qrcode 라이브러리를 사용해서 구현하기
- 결과물: repo 주소, 마켓 주소


<img src = "https://user-images.githubusercontent.com/65940401/163940168-2046d35d-a5a8-4147-b178-edcec3f5259f.png" width="30%" height="30%">

## Structure
    .
    ├── model             
    │     ├── Repository       # Repository class for handling data through Dao
    │     ├── QRCodeDatabase   # Database to store history
    │     ├── Result           # Database's entity class
    │     ├── ResultDao.       # Dao class to get, delete, add in database
    │     └── Type             # Enum class to present QR code type
    ├── presenter         
    │     └── Presenter        # Presenter class
    ├── view          
    │     ├── MainActivity     # Main Activity
    │     └── ResultAdapter    # Adapter class for recycler view
    └── Contract               # Contract class
    
## Library
- https://github.com/zxing/zxing
- https://developer.android.com/jetpack/androidx/releases/room
