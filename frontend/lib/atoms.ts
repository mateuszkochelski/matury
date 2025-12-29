import { atomWithStorage } from "jotai/utils";

export const favouritesAtom = atomWithStorage<number[]>("favourites", []);
