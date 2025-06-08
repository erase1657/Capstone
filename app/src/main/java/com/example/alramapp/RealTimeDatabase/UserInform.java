package com.example.alramapp.RealTimeDatabase;

public class UserInform {
    private String Email;
    private String Uid;
    private String Image;
    private String Name;
    private String Gender;
    private int Life;
    private int Score;


    public UserInform() {}


    //사용자 이메일
    public String getEmail(){ return Email; }
    public void setEmail(String email){ this.Email = email; }

    //사용자 Uid
    public String getUid() { return Uid; }
    public void setUid(String uid) { this.Uid = uid; }

    //캐릭턱 이미지
    public String getImage() { return Image; }
    public void setImage(String image) { this.Image = image; }

    //캐릭터 이름
    public String getName() { return Name; }
    public void setName(String name) { this.Name = name; }

    //캐릭터 성별
    public String getGender() { return Gender; }
    public void setGender(String gender) { this.Gender = gender; }

    //캐릭터 라이프 값
    public int getLife() { return Life; }
    public void setLife(int life) { this.Life = life; }

    //사용자 점수
    public int getScore() { return Score; }
    public void setScore(int score) { this.Score = score; }


}
