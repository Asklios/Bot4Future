package main.java.files.interfaces;

import net.dv8tion.jda.api.entities.Member;

public interface InviteDatabase {

    void saveVerified(Member specialMember, Member verifiedMember);
    void saveSpecialMember(Member specialMember);

}
