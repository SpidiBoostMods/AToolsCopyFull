package net.spidiboost.copyfull;
import java.util.regex.*;

final class History {
    private static Pattern rx(String s){return Pattern.compile(s,Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);}
    static final Pattern HEADER=rx("^История (персонала )?([a-z0-9_]{1,16}) +\\(Лимит: *(\\d{1,9})\\) *:? *(?:\\[CopyFull])?$");
    static final Pattern TIME=rx("^\\[\\d{1,2}:\\d{2}:\\d{2}]\\s*"), COLOR=rx("§[0-9a-fk-orx]");
    static final Pattern DATE=rx("^[-–—]+ *\\[\\d{4}-\\d{2}-\\d{2} [0-9:]+] *[-–—]+$");
    static final Pattern ACTION=rx("^[a-z0-9_]{1,16} (?:был[аи]? |отменил[аи]? |was (?!unbanned by |unmuted by )).+$");
    static final Pattern REV=rx("^[a-z0-9_]{1,16} was (?:unbanned|unmuted) by [a-z0-9_]{1,16}[.!]?$");
    static final Pattern REASON=rx("^(?:По причине|Причина|Reason):.*");
    static final Pattern END=rx("^(?:Окончание (?:в |через |бана:|мута:)|Expires in ).+");
    final String nick; final int requested; final boolean staff;
    private final StringBuilder text=new StringBuilder();
    long last; int expected=-1, dates, actions; boolean action, reason, needsReason;
    String error;
    History(String nick,int requested,boolean staff,long now){this.nick=nick;this.requested=requested;this.staff=staff;last=now;}
    void accept(String packet,long now){
        for(String raw:packet.split("\\R")){
            if(error!=null)return;
            String line=TIME.matcher(COLOR.matcher(raw).replaceAll("").strip()).replaceFirst("").strip();
            Matcher h=HEADER.matcher(line);
            if(h.matches()){
                if(expected>=0){error="Получен другой ответ истории.";return;}
                int count=Integer.parseInt(h.group(3));
                if(staff!=(h.group(1)!=null)||!nick.equalsIgnoreCase(h.group(2))||count>requested)continue;
                expected=count;
                line=line.replaceFirst(" *\\[CopyFull]$", "").stripTrailing();
            } else if(expected<0)continue;
            else if(dates==0 && line.matches("(?iu)(?:нет|не найдено) (?:истории|записей|наказаний)[.!]?")){expected=0;}
            else if(DATE.matcher(line).matches()){
                if(dates>0 && (!action || needsReason&&!reason)){error="Пропущена часть записи.";return;}
                dates++;action=reason=needsReason=false;
            } else if(dates>0 && !action && ACTION.matcher(line).matches()){
                action=true;actions++;needsReason=!line.toLowerCase(java.util.Locale.ROOT).contains("отменил");
            } else if(dates>0 && REV.matcher(line).matches()){
                if(!action){action=true;actions++;}
            } else if(action && REASON.matcher(line).matches()){reason=true;}
            else if(!action || !END.matcher(line).matches())continue;
            if(dates>expected||text.length()+line.length()>48_000_000){error="Ответ превышает ожидаемый размер.";return;}
            text.append(line).append('\n');last=now;
        }
    }
    boolean shortAnswer(long now){
        return expected>dates && error==null && actions==dates && (!needsReason||reason) && now-last>=6000;
    }
    boolean sameShortAnswer(History previous){
        return dates==previous.dates && expected==previous.expected && result().lines().filter(l->!END.matcher(l).matches()).toList()
            .equals(previous.result().lines().filter(l->!END.matcher(l).matches()).toList());
    }
    boolean ready(long now){
        return error==null && expected>=0 && dates==expected && actions==dates && (!needsReason||reason) && now-last>=2500;
    }
    String result(){return text.toString().stripTrailing();}
}
