import { useRef, useMemo } from "react";
import type { Cut, Episode } from "../../../hooks/useEditorState";

interface BottomTimelineProps {
  cuts: Cut[];
  episodes: Episode[];
  activeCutId: string;
  activeEpisodeId: string;
  onSelectCut: (id: string) => void;
  onSelectEpisode: (id: string) => void;
  onAddEpisode: () => void;
  onAddCut: () => void;
  onPreview: () => void;
  onSortCuts: () => void;
}

export default function BottomTimeline({
  cuts,
  episodes,
  activeCutId,
  activeEpisodeId,
  onSelectCut,
  onSelectEpisode,
  onAddEpisode,
  onAddCut,
  onPreview,
  onSortCuts,
}: BottomTimelineProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  // 현재 에피소드에 속한 컷만 필터링
  const episodeCuts = useMemo(
    () => cuts.filter((c) => c.episodeId === activeEpisodeId),
    [cuts, activeEpisodeId]
  );

  return (
    <section className="bg-[#111] border-t border-[#2a2a2a] flex flex-col shrink-0" style={{ height: 200 }}>
      {/* 에피소드 탭 */}
      <div className="flex items-center border-b border-[#2a2a2a] h-8 px-2 gap-0.5 shrink-0 overflow-x-auto">
        <i className="ri-film-line text-[#555] text-sm mr-1 shrink-0" />
        {episodes.map((ep) => (
          <button
            key={ep.id}
            onClick={() => {
              onSelectEpisode(ep.id);
            }}
            className={`px-3 h-6 rounded text-[10px] transition-colors cursor-pointer whitespace-nowrap ${
              ep.id === activeEpisodeId
                ? "bg-orange-500/20 text-orange-400 font-medium"
                : "text-[#666] hover:text-[#aaa] hover:bg-[#1e1e1e]"
            }`}
          >
            {ep.title}
          </button>
        ))}
        <button
          onClick={onAddEpisode}
          className="px-2 h-6 rounded text-[#555] hover:text-[#888] hover:bg-[#1e1e1e] transition-colors cursor-pointer text-xs flex items-center gap-1 whitespace-nowrap ml-1"
        >
          <i className="ri-add-line" />
          에피소드 추가
        </button>
        <div className="ml-auto flex items-center gap-2 shrink-0">
          <span className="text-[10px] text-[#555]">
            {episodeCuts.filter((c) => c.isGenerated).length}/{episodeCuts.length - 1} 컷 생성됨
          </span>
        </div>
      </div>

      {/* 컷 썸네일 스트립 */}
      <div className="flex-1 overflow-y-hidden">
        <div
          ref={scrollRef}
          className="flex items-center gap-2 h-full overflow-x-auto px-3 py-2 scroll-smooth"
          style={{ scrollbarWidth: "thin", scrollbarColor: "#333 transparent" }}
        >
          {episodeCuts.map((cut) => (
            <button
              key={cut.id}
              onClick={() => cut.isGenerated !== false || cut.index < 6 ? onSelectCut(cut.id) : undefined}
              className={`shrink-0 flex flex-col items-center gap-1 cursor-pointer group transition-all ${
                cut.id === activeCutId ? "scale-105" : "hover:scale-102"
              }`}
            >
              {/* 썸네일 */}
              <div
                className={`relative rounded-lg overflow-hidden border-2 transition-colors ${
                  cut.id === activeCutId
                    ? "border-orange-500 shadow-[0_0_12px_rgba(249,115,22,0.4)]"
                    : "border-[#2a2a2a] group-hover:border-[#444]"
                }`}
                style={{ width: 72, height: 100 }}
              >
                {cut.thumbnail ? (
                  <img
                    src={cut.thumbnail}
                    alt={cut.label}
                    className="w-full h-full object-cover"
                    draggable={false}
                  />
                ) : (
                  <div className="w-full h-full bg-[#1a1a1a] flex flex-col items-center justify-center gap-1">
                    {cut.isGenerated === false && cut.index < 6 ? (
                      <>
                        <i className="ri-sparkling-line text-[#444] text-xl" />
                        <span className="text-[8px] text-[#444]">생성 전</span>
                      </>
                    ) : (
                      <>
                        <i className="ri-add-line text-[#333] text-2xl" />
                        <span className="text-[8px] text-[#333]">컷 추가</span>
                      </>
                    )}
                  </div>
                )}

                {/* 활성 컷 인디케이터 */}
                {cut.id === activeCutId && (
                  <div className="absolute top-1 right-1 w-2 h-2 rounded-full bg-orange-500" />
                )}

                {/* 생성 완료 뱃지 */}
                {cut.isGenerated && (
                  <div className="absolute bottom-1 left-1 w-4 h-4 rounded-full bg-emerald-500/90 flex items-center justify-center">
                    <i className="ri-check-line text-white text-[8px]" />
                  </div>
                )}

                {/* 호버 오버레이 */}
                {cut.thumbnail && (
                  <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <i className="ri-edit-line text-white text-sm" />
                  </div>
                )}
              </div>

              {/* 컷 번호 */}
              <span className={`text-[9px] whitespace-nowrap ${cut.id === activeCutId ? "text-orange-400 font-bold" : "text-[#555]"}`}>
                {cut.label}
              </span>
            </button>
          ))}

          {/* 구분선 + 재생성 영역 */}
          <div className="shrink-0 flex flex-col items-center justify-center h-full px-2">
            <div className="w-px h-14 bg-[#2a2a2a]" />
          </div>

          {/* 컷 추가 버튼 */}
          <button
            onClick={onAddCut}
            className="shrink-0 flex flex-col items-center gap-1 cursor-pointer group"
          >
            <div
              className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-[#2a2a2a] group-hover:border-orange-500/40 transition-colors bg-[#111] group-hover:bg-orange-500/5"
              style={{ width: 72, height: 100 }}
            >
              <i className="ri-add-line text-[#333] group-hover:text-orange-500/60 text-2xl transition-colors" />
            </div>
            <span className="text-[9px] text-[#444] group-hover:text-[#666] transition-colors whitespace-nowrap">새 컷</span>
          </button>
        </div>
      </div>

      {/* 하단 상태 바 */}
      <div className="flex items-center justify-between px-4 h-7 border-t border-[#1e1e1e] shrink-0">
        <div className="flex items-center gap-4 text-[10px] text-[#555]">
          <span className="whitespace-nowrap">
            <i className="ri-scissors-cut-line mr-1" />
            {cuts.filter((c) => c.isGenerated).length}컷 완성
          </span>
          <span className="whitespace-nowrap">
            <i className="ri-time-line mr-1" />
            약 {episodeCuts.filter((c) => c.isGenerated).length * 3}분 분량
          </span>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={onPreview}
            className="flex items-center gap-1 text-[10px] text-[#555] hover:text-[#888] cursor-pointer transition-colors whitespace-nowrap"
          >
            <i className="ri-play-line" />
            미리보기
          </button>
          <button
            onClick={onSortCuts}
            className="flex items-center gap-1 text-[10px] text-[#555] hover:text-[#888] cursor-pointer transition-colors whitespace-nowrap"
          >
            <i className="ri-sort-asc" />
            컷 정렬
          </button>
        </div>
      </div>
    </section>
  );
}
