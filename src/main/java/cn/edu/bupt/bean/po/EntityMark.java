package cn.edu.bupt.bean.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.util.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "entity_mark")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "fieldHandler"})
@Data
@Slf4j
public class EntityMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "text")
    private String originContent;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "int default -1")
    private int passed;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int reviewed;

    @Column(name = "ver_date", columnDefinition = "date")
    private Date verDate;

    private String description;

    @Column(name = "verify_result", columnDefinition = "int default -1")
//    @Enumerated(EnumType.ORDINAL)
    private int verifyResult;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "stat_id")
    @JsonIgnore
    private VerifyStatement statement;

    public boolean updateVerifyResult(String newContent){
        if (this.content.equals(newContent)) { //没有发生修改
            if (this.passed == 0) this.setVerifyResult(VerifyResult.DENIED.ordinal()); //没有通过- 拒绝
            else this.setVerifyResult(VerifyResult.ACCEPT.ordinal()); // 通过 - 直接通过
        } else {  //发生了修改
            if (this.passed == 0) this.setVerifyResult(VerifyResult.MODIFY_DENIED.ordinal());
            else {
                this.setVerifyResult(VerifyResult.MODIFY_ACCEPT.ordinal());
            }
        }
        return (this.passed == 1); //只要是通过的情况就需要更新stmtEntities
    }

    private static String tagPatternTailStr = "</[on][a-z0-9]*>";
    private static Pattern tagPatternTail = Pattern.compile(tagPatternTailStr);
    private static String otherTagHead = "<o>";
    private static String otherTagTail = "</o>";
    private static String tagPatternHeadStr = "<[on][a-z0-9]*>";
    private static Pattern tagPatternHead = Pattern.compile(tagPatternHeadStr);
    public String getNonTagContent(){
        // 将所有的标签匹配去除掉
        Matcher matcher = tagPatternTail.matcher(this.content);
        return matcher.replaceAll("");
    }

    public String getFullTagContent(){
        // 对只有结束标签的字符串做补全
        Matcher matcher = tagPatternTail.matcher(this.content);
        Stack<Pair<Integer, String>> tailStack = new Stack<>();
        int end = 0; //最初的end是句子的首部
        while(matcher.find()){
            int start = matcher.start();
            String tag = this.content.substring(start, matcher.end());
            tailStack.push(new Pair<>(end, tag.replace("/","")));
            end = matcher.end();
        }

        String fullTagContent = "";
        end = this.content.length(); // 实际上若数据正确的话，上一个循环结束后end就应该在句子末尾
        while(!tailStack.empty()){
            Pair<Integer, String> tagUnit = tailStack.pop();
            int loc = tagUnit.getKey();
            String tag = tagUnit.getValue();
            if(tag.equals(otherTagHead))
                tag = "";
            String previous = this.content.substring(loc, end);
            fullTagContent = tag.concat(previous).concat(fullTagContent);
            end = loc;
        }
        return fullTagContent.replace(otherTagTail, "");
    }

    public static String recoverTagContent(String content){
        // 将前端返回的字符串还原回实体标注格式
        //把每个<n*> 改成</o> , 若结尾不是</n*>则加上</o>
        Matcher matcher = tagPatternHead.matcher(content);
        String tagContent = matcher.replaceAll(otherTagTail);
        String tagPattern = "</n[a-z0-9]+>$";

        Pattern pattern = Pattern.compile(tagPattern);
        matcher = pattern.matcher(tagContent);
        if(!matcher.find())
            tagContent = tagContent.concat(otherTagTail); //补上结尾的</o>

        boolean hasHead = otherTagTail.equals(tagContent.substring(0, 4));
        if(hasHead)
            tagContent = tagContent.substring(4); // 去除首部的<o>

        return tagContent.replace("></o>",">"); // 最后消除紧跟在别的实体后面的的</o>
    }

    public List<Pair<Integer, Pair<Integer, String>>> getEntitiesLoc(String content){
        List<Pair<Integer, Pair<Integer, String>>> entities = new ArrayList<>();
        //  类似getFullContent, 找</tag> 的位置，并构造出原始字符串
        Matcher matcher = tagPatternTail.matcher(content);

        int end = 0; //最初的end是句子的首部
        StringBuffer nonTagContent = new StringBuffer();
        StringBuffer entitiesStr = new StringBuffer(String.format("Entities %d includes:", this.id));
        while(matcher.find()){
            int start = matcher.start();
            String tag = content.substring(start, matcher.end());
            int nonTagStart = nonTagContent.length();
            nonTagContent.append(content.substring(end, start));
            int nonTagEnd = nonTagContent.length();
            if(!tag.equals(otherTagTail)) {  // 上一个end到这一个start之间是一个实体
                Pair<Integer, String> value = new Pair<>(nonTagEnd,tag);
                entities.add(new Pair<>(nonTagStart, value));
                entitiesStr.append(nonTagContent.toString().substring(nonTagStart, nonTagEnd) + ",");
            }
            end = matcher.end();
        }
        System.out.println(entitiesStr.toString());
        //System.out.println(nonTagContent.toString());
        return entities;
    }
}
