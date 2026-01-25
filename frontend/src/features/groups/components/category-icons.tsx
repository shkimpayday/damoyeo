// 카테고리별 문토 스타일 일러스트 아이콘

interface IconProps {
  className?: string;
}

export const CategoryIcons: Record<number, React.FC<IconProps>> = {
  // 1. 운동/스포츠 - 달리는 사람
  1: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      {/* 배경 */}
      <rect width="64" height="64" rx="16" fill="#FFF0F0"/>
      {/* 머리 */}
      <circle cx="38" cy="16" r="7" fill="#FFD93D"/>
      {/* 눈 */}
      <circle cx="36" cy="15" r="1.5" fill="#333"/>
      <circle cx="41" cy="15" r="1.5" fill="#333"/>
      {/* 볼터치 */}
      <circle cx="34" cy="18" r="2" fill="#FFB5B5" fillOpacity="0.6"/>
      {/* 입 */}
      <path d="M37 19C37 19 38.5 21 40 19" stroke="#333" strokeWidth="1.2" strokeLinecap="round"/>
      {/* 몸통 */}
      <ellipse cx="32" cy="32" rx="8" ry="10" fill="#FF6B6B"/>
      {/* 팔 */}
      <path d="M24 28C18 24 16 28 14 26" stroke="#FFD93D" strokeWidth="5" strokeLinecap="round"/>
      <path d="M40 28C46 32 50 28 52 30" stroke="#FFD93D" strokeWidth="5" strokeLinecap="round"/>
      {/* 다리 */}
      <path d="M28 42C24 50 20 52 18 50" stroke="#4ECDC4" strokeWidth="5" strokeLinecap="round"/>
      <path d="M36 42C42 48 48 46 50 48" stroke="#4ECDC4" strokeWidth="5" strokeLinecap="round"/>
      {/* 신발 */}
      <ellipse cx="17" cy="51" rx="4" ry="2.5" fill="#FF6B6B"/>
      <ellipse cx="51" cy="49" rx="4" ry="2.5" fill="#FF6B6B"/>
    </svg>
  ),

  // 2. 사교/인맥 - 웃는 얼굴들
  2: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#E8F5E9"/>
      {/* 왼쪽 얼굴 */}
      <circle cx="22" cy="28" r="12" fill="#FFD93D"/>
      <circle cx="19" cy="26" r="2" fill="#333"/>
      <circle cx="26" cy="26" r="2" fill="#333"/>
      <path d="M17 32C17 32 22 37 27 32" stroke="#333" strokeWidth="2" strokeLinecap="round"/>
      {/* 오른쪽 얼굴 */}
      <circle cx="42" cy="28" r="12" fill="#FFD93D"/>
      <circle cx="39" cy="26" r="2" fill="#333"/>
      <circle cx="46" cy="26" r="2" fill="#333"/>
      <path d="M37 32C37 32 42 37 47 32" stroke="#333" strokeWidth="2" strokeLinecap="round"/>
      {/* 하트 */}
      <path d="M32 48L28 44C24 40 24 36 28 34C30 33 32 34 32 36C32 34 34 33 36 34C40 36 40 40 36 44L32 48Z" fill="#FF6B6B"/>
      {/* 볼터치 */}
      <circle cx="16" cy="30" r="2.5" fill="#FFB5B5" fillOpacity="0.5"/>
      <circle cx="49" cy="30" r="2.5" fill="#FFB5B5" fillOpacity="0.5"/>
    </svg>
  ),

  // 3. 아웃도어/여행 - 텐트와 산
  3: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#E3F2FD"/>
      {/* 하늘 구름 */}
      <ellipse cx="14" cy="14" rx="6" ry="4" fill="white"/>
      <ellipse cx="50" cy="12" rx="8" ry="5" fill="white"/>
      {/* 태양 */}
      <circle cx="52" cy="16" r="6" fill="#FFD93D"/>
      {/* 뒷산 */}
      <path d="M0 50L20 26L40 50H0Z" fill="#81C784"/>
      {/* 앞산 */}
      <path d="M24 50L44 22L64 50H24Z" fill="#4CAF50"/>
      {/* 산 눈 */}
      <path d="M44 22L38 32H50L44 22Z" fill="white"/>
      {/* 텐트 */}
      <path d="M18 50L28 34L38 50H18Z" fill="#FF7043"/>
      <path d="M28 34L28 50" stroke="#E64A19" strokeWidth="2"/>
      {/* 텐트 입구 */}
      <path d="M24 50L28 42L32 50" fill="#5D4037"/>
      {/* 나무 */}
      <rect x="48" y="42" width="3" height="8" fill="#8D6E63"/>
      <circle cx="49.5" cy="38" r="6" fill="#66BB6A"/>
    </svg>
  ),

  // 4. 문화/공연 - 마스크/티켓
  4: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#F3E5F5"/>
      {/* 티켓 */}
      <rect x="10" y="18" width="44" height="28" rx="4" fill="#E91E63"/>
      <rect x="10" y="18" width="44" height="28" rx="4" stroke="#AD1457" strokeWidth="2"/>
      {/* 티켓 장식선 */}
      <path d="M10 30H54" stroke="#AD1457" strokeWidth="1" strokeDasharray="4 2"/>
      {/* 티켓 구멍 */}
      <circle cx="10" cy="32" r="4" fill="#F3E5F5"/>
      <circle cx="54" cy="32" r="4" fill="#F3E5F5"/>
      {/* ADMIT ONE */}
      <text x="32" y="26" fill="white" fontSize="6" fontWeight="bold" textAnchor="middle">ADMIT ONE</text>
      {/* 별 장식 */}
      <path d="M20 38L21 41L24 41L22 43L23 46L20 44L17 46L18 43L16 41L19 41L20 38Z" fill="#FFD93D"/>
      <path d="M32 38L33 41L36 41L34 43L35 46L32 44L29 46L30 43L28 41L31 41L32 38Z" fill="#FFD93D"/>
      <path d="M44 38L45 41L48 41L46 43L47 46L44 44L41 46L42 43L40 41L43 41L44 38Z" fill="#FFD93D"/>
    </svg>
  ),

  // 5. 음악/악기 - 음표와 헤드폰
  5: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#FFF8E1"/>
      {/* 헤드폰 밴드 */}
      <path d="M16 36C16 24 24 16 32 16C40 16 48 24 48 36" stroke="#FF9800" strokeWidth="4" strokeLinecap="round"/>
      {/* 왼쪽 이어폰 */}
      <rect x="10" y="32" width="10" height="16" rx="5" fill="#FF9800"/>
      <rect x="12" y="34" width="6" height="8" rx="3" fill="#FFE0B2"/>
      {/* 오른쪽 이어폰 */}
      <rect x="44" y="32" width="10" height="16" rx="5" fill="#FF9800"/>
      <rect x="46" y="34" width="6" height="8" rx="3" fill="#FFE0B2"/>
      {/* 음표 1 */}
      <circle cx="26" cy="52" r="4" fill="#4CAF50"/>
      <path d="M30 52V42" stroke="#4CAF50" strokeWidth="2"/>
      <path d="M30 42C30 42 34 40 34 44" stroke="#4CAF50" strokeWidth="2" strokeLinecap="round"/>
      {/* 음표 2 */}
      <circle cx="38" cy="50" r="3" fill="#2196F3"/>
      <path d="M41 50V42" stroke="#2196F3" strokeWidth="2"/>
      <path d="M41 42C41 42 44 40 44 43" stroke="#2196F3" strokeWidth="2" strokeLinecap="round"/>
    </svg>
  ),

  // 6. 외국어 - 말풍선
  6: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#E8EAF6"/>
      {/* 큰 말풍선 */}
      <path d="M8 12H42C44.2 12 46 13.8 46 16V32C46 34.2 44.2 36 42 36H20L12 44V36H8C5.8 36 4 34.2 4 32V16C4 13.8 5.8 12 8 12Z" fill="#5C6BC0"/>
      {/* 작은 말풍선 */}
      <path d="M56 24H30C27.8 24 26 25.8 26 28V40C26 42.2 27.8 44 30 44H48L54 50V44H56C58.2 44 60 42.2 60 40V28C60 25.8 58.2 24 56 24Z" fill="#7986CB"/>
      {/* 텍스트 */}
      <text x="25" y="27" fill="white" fontSize="10" fontWeight="bold">Hi!</text>
      <text x="38" y="37" fill="white" fontSize="8" fontWeight="bold">안녕!</text>
      {/* 지구본 */}
      <circle cx="14" cy="52" r="6" fill="#4FC3F7"/>
      <ellipse cx="14" cy="52" rx="2.5" ry="6" stroke="#29B6F6" strokeWidth="1"/>
      <path d="M8 52H20" stroke="#29B6F6" strokeWidth="1"/>
    </svg>
  ),

  // 7. 독서 - 펼친 책
  7: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#E0F7FA"/>
      {/* 책 왼쪽 */}
      <path d="M8 16C8 14 10 12 14 12C22 12 30 14 32 18V52C30 48 22 46 14 46C10 46 8 48 8 50V16Z" fill="#26A69A"/>
      {/* 책 오른쪽 */}
      <path d="M56 16C56 14 54 12 50 12C42 12 34 14 32 18V52C34 48 42 46 50 46C54 46 56 48 56 50V16Z" fill="#4DB6AC"/>
      {/* 책 라인 */}
      <path d="M14 22H26M14 28H24M14 34H26M14 40H22" stroke="#B2DFDB" strokeWidth="1.5" strokeLinecap="round"/>
      <path d="M38 22H50M40 28H50M38 34H50M42 40H50" stroke="#B2DFDB" strokeWidth="1.5" strokeLinecap="round"/>
      {/* 책갈피 */}
      <path d="M46 12V24L48 22L50 24V12" fill="#FF5722"/>
      {/* 하트 */}
      <path d="M32 8L30 6C28 4 28 2 30 2C31 2 32 3 32 4C32 3 33 2 34 2C36 2 36 4 34 6L32 8Z" fill="#E91E63"/>
    </svg>
  ),

  // 8. 스터디 - 졸업모와 책
  8: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#FFFDE7"/>
      {/* 책 스택 */}
      <rect x="14" y="40" width="36" height="8" rx="2" fill="#F44336"/>
      <rect x="16" y="32" width="32" height="8" rx="2" fill="#2196F3"/>
      <rect x="18" y="24" width="28" height="8" rx="2" fill="#4CAF50"/>
      {/* 졸업모 */}
      <path d="M32 8L52 16L32 24L12 16L32 8Z" fill="#37474F"/>
      <path d="M20 18V28C20 28 26 32 32 32C38 32 44 28 44 28V18" stroke="#37474F" strokeWidth="3"/>
      {/* 술 */}
      <path d="M52 16V24" stroke="#FFC107" strokeWidth="2"/>
      <circle cx="52" cy="26" r="3" fill="#FFC107"/>
      {/* 연필 */}
      <rect x="48" y="36" width="4" height="20" rx="1" fill="#FFB300" transform="rotate(15 48 36)"/>
      <path d="M52 54L54 58L50 58L52 54Z" fill="#FFE082" transform="rotate(15 52 56)"/>
    </svg>
  ),

  // 9. 게임/오락 - 게임 컨트롤러
  9: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#EDE7F6"/>
      {/* 컨트롤러 본체 */}
      <path d="M8 28C8 22 12 18 18 18H46C52 18 56 22 56 28V36C56 44 52 50 44 50H38C36 50 34 48 32 48C30 48 28 50 26 50H20C12 50 8 44 8 36V28Z" fill="#7E57C2"/>
      {/* 왼쪽 십자키 */}
      <rect x="14" y="30" width="4" height="12" rx="1" fill="#EDE7F6"/>
      <rect x="12" y="34" width="12" height="4" rx="1" fill="#EDE7F6"/>
      {/* 오른쪽 버튼들 */}
      <circle cx="44" cy="28" r="4" fill="#F44336"/>
      <circle cx="52" cy="34" r="4" fill="#4CAF50"/>
      <circle cx="44" cy="40" r="4" fill="#2196F3"/>
      <circle cx="36" cy="34" r="4" fill="#FFC107"/>
      {/* 가운데 버튼 */}
      <rect x="28" y="30" width="8" height="4" rx="2" fill="#5E35B1"/>
      {/* 조이스틱 */}
      <circle cx="20" cy="44" r="3" fill="#B39DDB"/>
      <circle cx="44" cy="44" r="3" fill="#B39DDB"/>
    </svg>
  ),

  // 10. 사진/영상 - 카메라
  10: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#E1F5FE"/>
      {/* 카메라 본체 */}
      <rect x="8" y="20" width="48" height="34" rx="6" fill="#37474F"/>
      {/* 플래시 */}
      <rect x="12" y="24" width="8" height="6" rx="2" fill="#78909C"/>
      {/* 렌즈 외곽 */}
      <circle cx="32" cy="38" r="14" fill="#263238"/>
      <circle cx="32" cy="38" r="11" fill="#455A64"/>
      {/* 렌즈 */}
      <circle cx="32" cy="38" r="8" fill="#29B6F6"/>
      <circle cx="32" cy="38" r="5" fill="#03A9F4"/>
      {/* 렌즈 반사 */}
      <circle cx="28" cy="34" r="2" fill="white" fillOpacity="0.6"/>
      {/* 뷰파인더 */}
      <path d="M24 14H40L44 20H20L24 14Z" fill="#455A64"/>
      {/* 셔터 버튼 */}
      <circle cx="50" cy="24" r="4" fill="#F44336"/>
      {/* 별 이펙트 */}
      <path d="M52 8L53 11L56 11L54 13L55 16L52 14L49 16L50 13L48 11L51 11L52 8Z" fill="#FFC107"/>
    </svg>
  ),

  // 11. 요리 - 프라이팬과 음식
  11: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#FFF3E0"/>
      {/* 프라이팬 */}
      <ellipse cx="28" cy="36" rx="20" ry="16" fill="#607D8B"/>
      <ellipse cx="28" cy="34" rx="16" ry="12" fill="#455A64"/>
      {/* 손잡이 */}
      <rect x="46" y="32" width="16" height="6" rx="3" fill="#8D6E63"/>
      {/* 계란 후라이 */}
      <ellipse cx="28" cy="34" rx="10" ry="8" fill="white"/>
      <circle cx="28" cy="33" r="5" fill="#FFC107"/>
      {/* 베이컨 */}
      <path d="M18 40C20 38 22 40 24 38C26 36 28 38 30 36" stroke="#E91E63" strokeWidth="3" strokeLinecap="round"/>
      {/* 김 */}
      <path d="M22 24C22 22 24 20 24 18" stroke="#90A4AE" strokeWidth="2" strokeLinecap="round"/>
      <path d="M28 22C28 20 30 18 30 16" stroke="#90A4AE" strokeWidth="2" strokeLinecap="round"/>
      <path d="M34 24C34 22 36 20 36 18" stroke="#90A4AE" strokeWidth="2" strokeLinecap="round"/>
      {/* 토마토 */}
      <circle cx="52" cy="50" r="6" fill="#F44336"/>
      <path d="M50 45L52 44L54 45" stroke="#4CAF50" strokeWidth="2" strokeLinecap="round"/>
    </svg>
  ),

  // 12. 공예 - 팔레트와 붓
  12: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#FCE4EC"/>
      {/* 팔레트 */}
      <ellipse cx="30" cy="36" rx="22" ry="18" fill="#8D6E63"/>
      <ellipse cx="30" cy="34" rx="20" ry="16" fill="#BCAAA4"/>
      {/* 물감들 */}
      <circle cx="18" cy="28" r="5" fill="#F44336"/>
      <circle cx="30" cy="24" r="5" fill="#FFC107"/>
      <circle cx="42" cy="28" r="5" fill="#2196F3"/>
      <circle cx="20" cy="40" r="4" fill="#4CAF50"/>
      <circle cx="38" cy="42" r="4" fill="#9C27B0"/>
      {/* 엄지 구멍 */}
      <ellipse cx="30" cy="38" rx="4" ry="5" fill="#FCE4EC"/>
      {/* 붓 */}
      <rect x="48" y="8" width="4" height="24" rx="2" fill="#FFE0B2" transform="rotate(30 48 8)"/>
      <path d="M54 6L60 20L52 24L48 10L54 6Z" fill="#5D4037"/>
      <path d="M55 8L59 18" stroke="#E91E63" strokeWidth="3" strokeLinecap="round"/>
    </svg>
  ),

  // 13. 자기계발 - 타겟과 화살
  13: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#E8F5E9"/>
      {/* 타겟 */}
      <circle cx="28" cy="32" r="22" fill="#F44336"/>
      <circle cx="28" cy="32" r="16" fill="white"/>
      <circle cx="28" cy="32" r="10" fill="#F44336"/>
      <circle cx="28" cy="32" r="4" fill="white"/>
      {/* 화살 */}
      <path d="M28 32L52 8" stroke="#795548" strokeWidth="3"/>
      <path d="M48 6L54 4L56 10L52 8L48 6Z" fill="#607D8B"/>
      {/* 화살 깃털 */}
      <path d="M30 30L26 34M32 28L28 32" stroke="#4CAF50" strokeWidth="2" strokeLinecap="round"/>
      {/* 체크마크 */}
      <path d="M44 44L48 50L56 40" stroke="#4CAF50" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
      {/* 별 */}
      <path d="M10 12L11 15L14 15L12 17L13 20L10 18L7 20L8 17L6 15L9 15L10 12Z" fill="#FFC107"/>
    </svg>
  ),

  // 14. 봉사활동 - 손과 하트
  14: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#FFEBEE"/>
      {/* 손 */}
      <path d="M20 54C16 54 12 50 12 44V32C12 28 14 26 18 26C20 26 22 28 22 30V20C22 18 24 16 26 16C28 16 30 18 30 20V18C30 16 32 14 34 14C36 14 38 16 38 18V20C38 18 40 16 42 16C44 16 46 18 46 20V44C46 50 42 54 38 54H20Z" fill="#FFCC80"/>
      {/* 하트 */}
      <path d="M32 10L28 6C24 2 24 -2 28 -2C30 -2 32 0 32 2C32 0 34 -2 36 -2C40 -2 40 2 36 6L32 10Z" fill="#E91E63"/>
      {/* 작은 하트들 */}
      <path d="M12 16L10 14C8 12 8 10 10 10C11 10 12 11 12 12C12 11 13 10 14 10C16 10 16 12 14 14L12 16Z" fill="#F48FB1"/>
      <path d="M52 20L50 18C48 16 48 14 50 14C51 14 52 15 52 16C52 15 53 14 54 14C56 14 56 16 54 18L52 20Z" fill="#F48FB1"/>
      <path d="M50 46L48 44C46 42 46 40 48 40C49 40 50 41 50 42C50 41 51 40 52 40C54 40 54 42 52 44L50 46Z" fill="#F48FB1"/>
    </svg>
  ),

  // 15. 반려동물 - 강아지
  15: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#FFF8E1"/>
      {/* 귀 */}
      <ellipse cx="16" cy="22" rx="8" ry="12" fill="#8D6E63"/>
      <ellipse cx="48" cy="22" rx="8" ry="12" fill="#8D6E63"/>
      {/* 얼굴 */}
      <circle cx="32" cy="32" r="18" fill="#BCAAA4"/>
      {/* 이마 */}
      <ellipse cx="32" cy="24" rx="8" ry="6" fill="#8D6E63"/>
      {/* 눈 */}
      <circle cx="24" cy="30" r="4" fill="#333"/>
      <circle cx="40" cy="30" r="4" fill="#333"/>
      <circle cx="25" cy="29" r="1.5" fill="white"/>
      <circle cx="41" cy="29" r="1.5" fill="white"/>
      {/* 코 */}
      <ellipse cx="32" cy="38" rx="5" ry="4" fill="#4E342E"/>
      <ellipse cx="32" cy="37" rx="2" ry="1" fill="#6D4C41"/>
      {/* 입 */}
      <path d="M28 42C28 42 32 46 36 42" stroke="#4E342E" strokeWidth="2" strokeLinecap="round"/>
      {/* 볼터치 */}
      <circle cx="18" cy="36" r="3" fill="#FFAB91" fillOpacity="0.5"/>
      <circle cx="46" cy="36" r="3" fill="#FFAB91" fillOpacity="0.5"/>
      {/* 혀 */}
      <ellipse cx="32" cy="46" rx="3" ry="4" fill="#EF5350"/>
    </svg>
  ),

  // 16. IT/개발 - 노트북과 코드
  16: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#E3F2FD"/>
      {/* 노트북 화면 */}
      <rect x="8" y="12" width="48" height="32" rx="4" fill="#37474F"/>
      <rect x="12" y="16" width="40" height="24" rx="2" fill="#263238"/>
      {/* 코드 */}
      <text x="16" y="26" fill="#4CAF50" fontSize="6" fontFamily="monospace">&lt;/&gt;</text>
      <path d="M16 30H36" stroke="#64B5F6" strokeWidth="2" strokeLinecap="round"/>
      <path d="M16 34H28" stroke="#BA68C8" strokeWidth="2" strokeLinecap="round"/>
      <path d="M32 34H44" stroke="#FFB74D" strokeWidth="2" strokeLinecap="round"/>
      {/* 노트북 받침대 */}
      <path d="M4 44H60L56 52H8L4 44Z" fill="#78909C"/>
      <rect x="24" y="44" width="16" height="2" rx="1" fill="#546E7A"/>
      {/* 커서 깜빡임 */}
      <rect x="46" y="26" width="2" height="8" fill="#4CAF50"/>
      {/* 로켓 */}
      <path d="M50 8L54 4L58 8L54 16L50 8Z" fill="#FF5722"/>
      <circle cx="54" cy="8" r="2" fill="#FFEB3B"/>
    </svg>
  ),

  // 17. 금융/재테크 - 돼지저금통
  17: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#FFF9C4"/>
      {/* 돼지 몸통 */}
      <ellipse cx="32" cy="36" rx="20" ry="16" fill="#F48FB1"/>
      {/* 귀 */}
      <ellipse cx="18" cy="24" rx="6" ry="8" fill="#F48FB1"/>
      <ellipse cx="18" cy="24" rx="3" ry="5" fill="#EC407A"/>
      <ellipse cx="46" cy="24" rx="6" ry="8" fill="#F48FB1"/>
      <ellipse cx="46" cy="24" rx="3" ry="5" fill="#EC407A"/>
      {/* 코 */}
      <ellipse cx="50" cy="36" rx="6" ry="5" fill="#EC407A"/>
      <circle cx="48" cy="35" r="1.5" fill="#333"/>
      <circle cx="52" cy="35" r="1.5" fill="#333"/>
      {/* 눈 */}
      <circle cx="38" cy="30" r="3" fill="#333"/>
      <circle cx="39" cy="29" r="1" fill="white"/>
      {/* 동전 슬롯 */}
      <rect x="26" y="22" width="12" height="3" rx="1.5" fill="#333"/>
      {/* 다리 */}
      <rect x="18" y="48" width="6" height="8" rx="3" fill="#F48FB1"/>
      <rect x="40" y="48" width="6" height="8" rx="3" fill="#F48FB1"/>
      {/* 동전 */}
      <circle cx="14" cy="14" r="6" fill="#FFC107"/>
      <text x="14" y="17" fill="#FF8F00" fontSize="8" fontWeight="bold" textAnchor="middle">$</text>
      {/* 반짝이 */}
      <path d="M52 10L53 13L56 13L54 15L55 18L52 16L49 18L50 15L48 13L51 13L52 10Z" fill="#FFC107"/>
    </svg>
  ),

  // 18. 기타 - 물음표와 별
  18: ({ className }) => (
    <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="16" fill="#ECEFF1"/>
      {/* 큰 물음표 */}
      <circle cx="32" cy="24" r="14" fill="#78909C"/>
      <path d="M26 20C26 16 30 14 34 14C38 14 42 16 42 22C42 26 38 28 34 30V34" stroke="white" strokeWidth="4" strokeLinecap="round"/>
      <circle cx="34" cy="42" r="3" fill="white"/>
      {/* 별들 */}
      <path d="M12 16L13 19L16 19L14 21L15 24L12 22L9 24L10 21L8 19L11 19L12 16Z" fill="#FFC107"/>
      <path d="M52 40L53 43L56 43L54 45L55 48L52 46L49 48L50 45L48 43L51 43L52 40Z" fill="#FFC107"/>
      <path d="M14 48L15 50L17 50L15.5 51.5L16 54L14 52.5L12 54L12.5 51.5L11 50L13 50L14 48Z" fill="#FFB74D"/>
      {/* 작은 원들 */}
      <circle cx="50" cy="16" r="4" fill="#90CAF9"/>
      <circle cx="54" cy="24" r="3" fill="#A5D6A7"/>
      <circle cx="10" cy="36" r="3" fill="#CE93D8"/>
    </svg>
  ),
};

// 기본 아이콘 (ID가 없을 때)
export const DefaultCategoryIcon: React.FC<IconProps> = ({ className }) => (
  <svg className={className} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect width="64" height="64" rx="16" fill="#F5F5F5"/>
    <circle cx="32" cy="32" r="12" fill="#BDBDBD"/>
    <circle cx="28" cy="30" r="2" fill="white"/>
  </svg>
);
