package com.ssafy.backend.board.dto;


import com.ssafy.backend.board.domain.Board;
import com.ssafy.backend.board.domain.Tag;
import com.ssafy.backend.common.utils.BoardValidator;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import com.ssafy.backend.common.exception.MyException;

public class BoardCreateRequestDto {

    private String boardTitle;
    private String boardContent;
    private int tagId;


    public String getBoardTitle() {
        return boardTitle;
    }

    public void setBoardTitle(String boardTitle) throws MyException {
        if(!BoardValidator.isValidTitle(boardTitle))
            throw new MyException("잘못된 제목 요청", HttpStatus.BAD_REQUEST);
        this.boardTitle = boardTitle;
    }

    public String getBoardContent() {
        return boardContent;
    }

    public void setBoardContent(String boardContent) throws MyException{
        if(!BoardValidator.isValidContent(boardContent))
            throw new MyException("잘못된 내용 요청", HttpStatus.BAD_REQUEST);
        this.boardContent = boardContent;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public Board toEntity(Tag tag, String userId) {
        return Board.builder().userId("test")
                .boardContent(boardContent)
                .boardTitle(boardTitle)
                .tagId(tag)
                .userId(userId).build();
    }

    public BoardCreateRequestDto(){

    }
    @Builder
    public BoardCreateRequestDto(String boardTitle,String boardContent,int tagId){
        setBoardTitle(boardTitle);
        setBoardContent(boardContent);
        setTagId(tagId);

    }
}
