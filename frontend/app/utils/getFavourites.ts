export function getFavourites(): number[] {
  const stored = localStorage.getItem("favourites");
  const favourites: number[] = stored ? JSON.parse(stored) : [];
  return favourites;
}
