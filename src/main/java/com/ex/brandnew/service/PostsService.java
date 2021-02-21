package com.ex.brandnew.service;

import com.ex.brandnew.domain.posts.Posts;
import com.ex.brandnew.domain.posts.PostsRepository;
import com.ex.brandnew.web.dto.PostsResponseDto;
import com.ex.brandnew.web.dto.PostsSaveRequestDto;
import com.ex.brandnew.web.dto.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class PostsService {

    private final PostsRepository postsRepository;

    @Transactional
    public Long save(PostsSaveRequestDto postsSaveRequestDto){
        return postsRepository.save(postsSaveRequestDto.toEntity()).getId();
    }

    public PostsResponseDto findById(Long id){
        Posts entity = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id="+ id));
        return new PostsResponseDto(entity);
    }

    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto){
        Posts entity = postsRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("잘못된 id 입니다.")
                );
        entity.update(requestDto.getTitle(), requestDto.getContent(), requestDto.getAuthor());

        return id;
    }
}
