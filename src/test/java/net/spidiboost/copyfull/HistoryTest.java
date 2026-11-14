package net.spidiboost.copyfull;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HistoryTest {
    @Test void bothCommandsPreserveLongMultilineOutputAndIgnoreChat(){
        for(boolean staff:new boolean[]{false,true}){
            History h=new History("Alex",1000,staff,0);
            String header="История "+(staff?"персонала ":"")+"Alex (Лимит: 1000):";
            StringBuilder expected=new StringBuilder(header);
            h.accept("История Another (Лимит: 1):",0);
            h.accept(header,1);
            for(int i=0;i<1000;i++){
                String data="-- [2026-11-14 14:00] --\nAlex был забанен куратором Staff\nПо причине: 'тест » "+i+"' [Истек]\nAlex was unbanned by ReallyWorld.";
                h.accept("AC » шорох",i);h.accept(data,i);expected.append('\n').append(data);
                if(i==700)assertFalse(h.ready(i+8000));
            }
            h.accept("Окончание в 2 дня.",3100);
            assertFalse(h.ready(4000));assertTrue(h.ready(5601));
            assertEquals(expected+"\nОкончание в 2 дня.",h.result());
        }
    }
    @Test void shorterHistoryNeedsASecondMatchingCompleteResponse(){
        String start="История Alex (Лимит: 1000):\n-- [2026-11-14 14:00] --\nAlex был заткнут куратором Staff\n";
        var first=new History("Alex",1000,false,0);first.accept(start+"По причине: flood",0);
        var again=new History("Alex",1000,false,1);again.accept(start+"По причине: flood",1);
        assertFalse(first.ready(9000));assertTrue(first.shortAnswer(9000));assertTrue(again.sameShortAnswer(first));
        var changed=new History("Alex",1000,false,0);changed.accept(start+"По причине: caps",0);
        assertFalse(changed.sameShortAnswer(first));
        var cut=new History("Alex",1000,false,0);cut.accept(start,0);assertFalse(cut.shortAnswer(30000));
    }
    @Test void incompleteOrInterruptedResponseCannotBeCopied(){
        History h=new History("Alex",2,false,0);
        h.accept("История Alex (Лимит: 2):\n-- [2026-11-10 10:00] --\nAlex был заткнут куратором Test",100);
        assertFalse(h.ready(90000));
        h.accept("История персонала Other (Лимит: 1):",90100);
        assertNotNull(h.error);assertFalse(h.ready(100000));
    }
}
