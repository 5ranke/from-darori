package com.daroring.fromdarori.domain.residentreport.service;

import com.daroring.fromdarori.domain.admin.repository.AdminUserRepository;
import com.daroring.fromdarori.domain.category.repository.CategoryRepository;
import com.daroring.fromdarori.domain.residentreport.dto.*;
import com.daroring.fromdarori.domain.residentreport.entity.ResidentReport;
import com.daroring.fromdarori.domain.residentreport.entity.ResidentReportStatus;
import com.daroring.fromdarori.domain.residentreport.repository.AdminResidentReportRepository;
import com.daroring.fromdarori.domain.story.dto.StoryCreateRequest;
import com.daroring.fromdarori.domain.story.dto.StoryImageRequest;
import com.daroring.fromdarori.domain.story.dto.StoryImageResponse;
import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StoryImage;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.story.repository.AdminStoryImageRepository;
import com.daroring.fromdarori.domain.story.repository.AdminStoryRepository;
import com.daroring.fromdarori.domain.story.service.LocalStoryImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminResidentReportService {
    private final AdminResidentReportRepository adminResidentReportRepository;
    private final CategoryRepository categoryRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminStoryRepository adminStoryRepository;
    private final AdminStoryImageRepository adminStoryImageRepository;
    private final LocalStoryImageStorage imageStorage;

    @Transactional(readOnly = true)
    public ReportInfoResponse getReports(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ResidentReport> reportPage = adminResidentReportRepository.findByStatus(ResidentReportStatus.PENDING, pageable);
        return new ReportInfoResponse(
                reportPage.getContent().stream()
                        .map(ReportItemResponse::from)
                        .toList(),
                reportPage.getNumber(),
                reportPage.getSize(),
                reportPage.getTotalPages(),
                Math.toIntExact(reportPage.getTotalElements()),
                reportPage.hasNext()
        );
    }

    @Transactional(readOnly = true)
    public ReportIdInfoResponse getReport(UUID reportId) {
        ResidentReport report = adminResidentReportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPORT4041", "제보를 찾을 수 없습니다."));
        return ReportIdInfoResponse.from(report);
    }

    @Transactional
    public ReportToStoryResponse createStory(StoryCreateRequest request, UUID adminId, UUID reportId) {
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,"CATEGORY4041","카테고리를 찾을 수 없습니다."));
        var admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ADMIN4041","운영자를 찾을 수 없습니다."));
        var report = adminResidentReportRepository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPORT4041", "제보를 찾을 수 없습니다."));

        if (report.getStatus() != ResidentReportStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "REPORT4091", "이미 소식에 등록되었습니다.");
        }
        report.update(admin);

        Story story = adminStoryRepository.save(Story.createFromReport(
                category, admin, request.title(), request.body(),
                request.actionUrl(), request.applicationDeadline(), report));

        List<StoryImage> savedImages = request.images() == null
                ? List.of()
                : saveNewImages(story, request.images());
        adminStoryRepository.flush();
        return ReportToStoryResponse.from(story, report, imageResponses(savedImages));
    }

    @Transactional
    public ReportDeleteResponse delete(UUID reportId) {
        ResidentReport report = adminResidentReportRepository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPORT4041", "제보를 찾을 수 없습니다."));
        if (report.getStatus() == ResidentReportStatus.APPROVED) {
            throw new ApiException(HttpStatus.CONFLICT, "REPORT4091", "이미 소식에 등록되었습니다.");
        } else {
            adminResidentReportRepository.delete(report);
        }
        return new ReportDeleteResponse(report.getId());
    }

    private List<StoryImage> saveNewImages(Story story, List<StoryImageRequest> requests) {
        Set<Integer> orders = new HashSet<>();
        Set<String> keys = new HashSet<>();
        var images = new ArrayList<StoryImage>(requests.size());
        for (var request : requests) {
            if (!orders.add(request.displayOrder()) || !keys.add(request.objectKey())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4003", "사진이나 표시 순서가 중복됐습니다.");
            }
            imageStorage.requireUploaded(request.objectKey());
            images.add(StoryImage.create(story, request.objectKey(), request.displayOrder()));
        }
        return adminStoryImageRepository.saveAll(images);
    }

    private List<StoryImageResponse> imageResponses(List<StoryImage> images) {
        return images.stream()
                .sorted(Comparator.comparing(StoryImage::getDisplayOrder))
                .map(image -> new StoryImageResponse(image.getId(), image.getDisplayOrder()))
                .toList();
    }
}
