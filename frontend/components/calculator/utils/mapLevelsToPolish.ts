export const mapLevelToPolish = (level: string) => {
  const levels: Record<string, string> = {
    BASIC: "PODSTAWOWY",
    EXTENDED: "ROZSZERZONY",
    BILINGUAL: "DWUJĘZYCZNY",
  };
  return levels[level] ?? level;
};
