package net.spidiboost.copyfull;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
public final class CopyClient implements ClientModInitializer {
    private static History current, previous;
    public static boolean active(){return current!=null;}
    static long now(){return System.nanoTime()/1_000_000L;}
    public static void receive(String s){if(current!=null)current.accept(s,now());}
    static void say(String s){var p=MinecraftClient.getInstance().player;if(p!=null)p.sendMessage(Text.literal("[CopyFull] "+s),false);}
    public static void start(String nick,String count,boolean staff){
        var c=MinecraftClient.getInstance();
        if(c.getNetworkHandler()==null)return;
        if(current!=null){say("Уже загружается история "+current.nick+".");return;}
        if(!nick.matches("[a-zA-Z0-9_]{1,16}")||!count.matches("[1-9][0-9]{0,8}")){say("Формат: .copyfull ник количество true/false");return;}
        previous=null;
        current=new History(nick,Integer.parseInt(count),staff,now());
        c.getNetworkHandler().sendChatCommand((staff?"shist ":"hist ")+nick+" "+count);
        say("Запрошена история "+nick+".");
    }
    public void onInitializeClient(){
        ClientPlayConnectionEvents.DISCONNECT.register((handler,c)->{current=null;previous=null;});
        ClientTickEvents.END_CLIENT_TICK.register(c->{
            History h=current;if(h==null)return;
            long t=now();
            boolean verifiedShort=false;
            if(h.shortAnswer(t)){
                if(previous==null){
                    previous=h;current=new History(h.nick,h.requested,h.staff,t);
                    c.getNetworkHandler().sendChatCommand((h.staff?"shist ":"hist ")+h.nick+" "+h.requested);
                    return;
                }
                verifiedShort=h.sameShortAnswer(previous);
                if(!verifiedShort)h.error="Повторный ответ сервера отличается.";
            }
            if(h.ready(t)||verifiedShort){
                previous=null;
                if(h.dates==0){current=null;say("История "+h.nick+" пуста. Буфер не изменён.");return;}
                current=null; String data=h.result();
                c.keyboard.setClipboard(data);
                say(data.equals(c.keyboard.getClipboard())?"Скопировано: "+h.nick+" · "+h.actions:"Не удалось записать историю в буфер.");
            }else if(h.error!=null||t-h.last>=30000){
                current=null;previous=null;say((h.error!=null?h.error:h.expected<0?"Нет ответа сервера.":"Получено "+h.actions+" из "+h.expected+".")+" Буфер не изменён.");
            }
        });
    }
}
